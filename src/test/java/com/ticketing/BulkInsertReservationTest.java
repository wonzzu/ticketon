package com.ticketing;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.Member;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate; // 👈 추가

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
// ⚠️ 클래스 레벨의 @Transactional은 제거된 상태를 유지하여 OOM을 방지합니다.
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/ticketing?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&rewriteBatchedStatements=true",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.username=${DB_USERNAME:root}",
        "spring.datasource.password=${DB_PASSWORD:qwer1234}",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
        "spring.jpa.show-sql=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BulkInsertReservationTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TransactionTemplate txTemplate; // 👈 프로그래밍 방식의 트랜잭션 제어를 위해 주입

    private Long memberId;
    private Long scheduleId;

    @BeforeEach
    void setup() {
        // 🛠️ TransactionTemplate을 사용하여 셋업 로직을 실제 트랜잭션 안에서 실행합니다.
        txTemplate.execute(status -> {
            Seller seller = Seller.create("bench-seller@t.com", "pw", "벤치셀러", "010-0000-0000",
                    new Address("서울", "벤치로", "00000"), "벤치컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            NormalMember member = NormalMember.create("bench@t.com", "pw", "벤치회원", "벤치닉",
                    LocalDate.of(1990, 1, 1), Gender.MALE, "010-0000-0001", new Address("서울", "벤치로", "00000"));
            em.persist(member);

            Venue venue = Venue.create("벤치홀", new Address("서울", "벤치로", "00000"), 1, 1);
            em.persist(venue);

            Event event = Event.create("벤치공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            em.persist(event);

            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(schedule);

            em.flush();

            memberId = member.getId();
            scheduleId = schedule.getId();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        // 1. 안전한 데이터 청소를 위해 임시로 외래 키(FK) 체크를 끕니다.
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 2. 관련 테이블들을 모두 비워줍니다. (중복 에러의 원인 완벽 차단)
        jdbc.execute("TRUNCATE TABLE reservation_seat"); // 혹시 자식 테이블이 있다면 추가
        jdbc.execute("TRUNCATE TABLE reservation");
        jdbc.execute("TRUNCATE TABLE event_schedule");
        jdbc.execute("TRUNCATE TABLE event");
        jdbc.execute("TRUNCATE TABLE venue");
        jdbc.execute("TRUNCATE TABLE member");
        jdbc.execute("TRUNCATE TABLE seller");

        // 3. 청소가 끝난 후 외래 키 체크를 다시 켭니다.
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    void bench_1k() {
        bench(100);
    }

    @Test
    void bench_10k() {
        bench(1000);
    }

    private void bench(int n) {
        long jpaMs = time(() -> insertJpa(n));
        long jdbcMs = time(() -> insertJdbc(n));

        double ratio = jdbcMs == 0 ? 0 : (double) jpaMs / jdbcMs;
        System.out.printf("%n=== [%,d건] JPA saveAll = %,d ms / JDBC batch = %,d ms → JDBC가 약 %.1f배 빠름 ===%n",
                n, jpaMs, jdbcMs, ratio);
    }

    // JPA 1000건 단위로 트랜잭션을 완전히 쪼개서 반영 후 캐시 초기화
    private void insertJpa(int n) {
        for (int i = 0; i < n; i++) {
            final int index = i;
            // 건별 persist도 하나의 독립된 트랜잭션으로 보장
            txTemplate.execute(status -> {
                Member m = em.getReference(NormalMember.class, memberId);
                EventSchedule sch = em.getReference(EventSchedule.class, scheduleId);
                em.persist(Reservation.create(m, sch, "jpa-" + index, 10000));
                return null;
            });

            // 1000건마다 영속성 컨텍스트 완전히 비우기 (메모리 해제)
            if (i % 1000 == 999) {
                em.clear();
            }
        }
    }

    // JDBC Batch 쪼개기 기법 적용 (OOM 방지)
    private void insertJdbc(int n) {
        LocalDateTime now = LocalDateTime.now();
        int batchSize = 5000;
        List<Object[]> args = new ArrayList<>(batchSize);

        for (int i = 0; i < n; i++) {
            args.add(new Object[]{memberId, scheduleId, "jdbc-" + i, 10000, "PENDING", now, now, "bench"});

            if ((i + 1) % batchSize == 0) {
                executeBatch(args);
                args.clear();
            }
        }

        if (!args.isEmpty()) {
            executeBatch(args);
        }
    }

    private void executeBatch(List<Object[]> args) {
        jdbc.batchUpdate(
                "INSERT INTO reservation " +
                        "(member_id, schedule_id, idempotency_key, total_price, status, created_at, updated_at, created_by) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                args);
    }

    private long time(Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        return System.currentTimeMillis() - start;
    }
}