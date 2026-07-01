package com.ticketing.event.service;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.Seller;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 성능(캐시) — 공연 상세 find(id)는 조회 빈도가 높은데 매번 DB를 친다.
 * @Cacheable 적용 후에는 2번째 조회가 캐시 히트로 DB 쿼리 0이 된다(Green).
 * 현재는 캐시가 비활성(주석)이라 2번째도 DB 조회가 나가므로 Red.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("성능 - 공연 상세 캐시")
class EventCacheTest {

    @Autowired EventService eventService;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManagerFactory emf;
    @PersistenceContext EntityManager em;

    private Long eventId;

    @BeforeEach
    void setup() {
        tx.execute(s -> {
            Seller seller = Seller.create("cache-seller@t.com", "pw", "셀러", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            Event event = Event.create("캐시공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            event.approve();   // find()는 APPROVED만 조회 가능
            em.persist(event);
            eventId = event.getId();

            em.flush();
            return null;
        });
    }

    @Test
    @DisplayName("같은 공연 2회 조회 → 2번째는 캐시 히트로 쿼리 0")
    void 공연상세_캐시() {
        // given : 1번째 조회 (캐시 적용 시 이때 캐시에 적재)
        eventService.find(eventId);

        // when : 2번째 조회 직전 통계 초기화 후 재조회
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        eventService.find(eventId);

        // then : 캐시 히트면 2번째는 DB 쿼리 0
        long secondCount = stats.getPrepareStatementCount();
        System.out.printf("%n========== [공연 상세 캐시] ==========%n" +
                        "  2번째 조회 쿼리 수 : %d (캐시 히트면 0)%n" +
                        "====================================%n",
                secondCount);

        assertThat(secondCount).isEqualTo(0);   // 캐시 적용 후 Green. 현재는 캐시 비활성이라 Red
    }
}
