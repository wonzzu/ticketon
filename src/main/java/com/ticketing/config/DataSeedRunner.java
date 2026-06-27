package com.ticketing.config;

import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventReviewHistory;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.domain.EventSeat;
import com.ticketing.event.domain.EventStatus;
import com.ticketing.event.domain.ReviewAction;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.EventReviewHistoryRepository;
import com.ticketing.event.repository.EventScheduleRepository;
import com.ticketing.event.repository.EventSeatRepository;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.AdminMember;
import com.ticketing.member.domain.Gender;
import com.ticketing.member.domain.NormalMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.member.repository.AdminMemberRepository;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.member.repository.NormalMemberRepository;
import com.ticketing.member.repository.SellerRepository;
import com.ticketing.venue.domain.Seat;
import com.ticketing.venue.domain.SeatGrade;
import com.ticketing.venue.domain.SeatGradeRange;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.repository.SeatRepository;
import com.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ticketing.member.domain.Member;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationHistory;
import com.ticketing.reservation.domain.ReservationSeat;
import com.ticketing.reservation.repository.ReservationHistoryRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentHistory;
import com.ticketing.payment.repository.PaymentHistoryRepository;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.review.domain.Review;
import com.ticketing.review.repository.ReviewRepository;
import com.ticketing.coupon.domain.Coupon;
import com.ticketing.coupon.domain.DiscountType;
import com.ticketing.coupon.repository.CouponRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Random;
import com.ticketing.statistics.domain.DailySalesStats;
import com.ticketing.statistics.domain.DailyEventStats;
import com.ticketing.statistics.repository.DailySalesStatsRepository;
import com.ticketing.statistics.repository.DailyEventStatsRepository;
import com.ticketing.member.domain.MemberHistory;
import com.ticketing.member.domain.MemberStatus;
import com.ticketing.member.repository.MemberHistoryRepository;

/**
 * 로컬 환경 더미 데이터 시드.
 * - spring.profiles.active=local 일 때만 활성화
 * - admin@test.com 이 이미 있으면 건너뜀 (멱등성)
 * - 비밀번호는 모두 "test1234"
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeedRunner implements CommandLineRunner {

    private static final String DEFAULT_PW = "test1234";

    // 측정엔 10만이면 충분(인덱스 EXPLAIN·N+1 차이 극명). 100만은 단일 트랜잭션이라 메모리 폭발 → 10만 유지
    private static final int PERF_MEMBERS = 10000;
    private static final int PERF_RESERVATIONS = 500000;
    private static final int PERF_HEAVY_RESERVATIONS = 500;
    private static final int REVIEWS_PER_EVENT = 150;

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final AdminMemberRepository adminMemberRepository;
    private final SellerRepository sellerRepository;
    private final NormalMemberRepository normalMemberRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventReviewHistoryRepository historyRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final StringRedisTemplate redisTemplate;
    private final DailySalesStatsRepository dailySalesStatsRepository;
    private final DailyEventStatsRepository dailyEventStatsRepository;
    private final MemberHistoryRepository memberHistoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) {
        if (memberRepository.existsByEmail("admin@test.com")) {
            log.info("[Seed] 이미 시드 데이터 존재 → 건너뜀");
            return;
        }

        log.info("[Seed] 더미 데이터 생성 시작");

        // JPA 시드(회원·공연·좌석·풀·리뷰·통계·이력)를 한 트랜잭션으로 묶어 커밋한다.
        // → 예매 JDBC batch가 FK(회원·좌석)를 참조하려면 그 데이터가 먼저 커밋돼 있어야 하므로.
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        PerfPool pool = tx.execute(status -> seedJpaPart());

        // 예매·결제 대량 적재만 JDBC batch + 청크 커밋 → 단일 트랜잭션 미커밋 누적(메모리 폭발) 회피
        seedPerfReservationsJdbc(tx, pool);

        log.info("[Seed] 더미 데이터 생성 완료");
    }

    // 회원·공연·좌석·소량 예매·리뷰·통계·이력 = JPA. TransactionTemplate 트랜잭션 안에서 실행되어 커밋된다.
    private PerfPool seedJpaPart() {
        // 1) 회원
        AdminMember admin = seedAdmin();
        Seller seller1 = seedSeller("seller1@test.com", "엔터테인먼트A", "김셀러", "111-11-11111");
        Seller seller2 = seedSeller("seller2@test.com", "프로덕션B", "박셀러", "222-22-22222");
        seedNormalMember();

        // 2) 공연장 + 좌석 (등급: VIP / R / S / A 4등급, 행 8이면 2행씩)
        Venue venue1 = seedVenue("잠실 종합운동장", 8, 10);
        Venue venue2 = seedVenue("블루스퀘어 신한카드홀", 8, 8);
        Venue venue3 = seedVenue("예술의전당 콘서트홀", 8, 9);

        Map<SeatGrade, Integer> price1 = Map.of(
                SeatGrade.VIP, 198000, SeatGrade.R, 165000, SeatGrade.S, 132000, SeatGrade.A, 99000);
        Map<SeatGrade, Integer> price2 = Map.of(
                SeatGrade.VIP, 154000, SeatGrade.R, 121000, SeatGrade.S,  99000, SeatGrade.A, 77000);
        Map<SeatGrade, Integer> price3 = Map.of(
                SeatGrade.VIP, 110000, SeatGrade.R,  88000, SeatGrade.S,  66000, SeatGrade.A, 44000);

        // 3) APPROVED 공연 (회차 자동 생성 → 메인 노출)
        seedApprovedEvent(seller1, admin, venue1, "아이유 콘서트 H.E.R.", "concert1",
                Category.CONCERT, AgeLimit.ALL,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                LocalDateTime.of(2026, 7, 1, 19, 30), price1);

        seedApprovedEvent(seller1, admin, venue1, "BTS WORLD TOUR 2026", "concert2",
                Category.CONCERT, AgeLimit.ALL,
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 17),
                LocalDateTime.of(2026, 8, 15, 19, 0), price1);

        seedApprovedEvent(seller2, admin, venue2, "뮤지컬 레미제라블", "musical1",
                Category.MUSICAL, AgeLimit.AGE_12,
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 8, 31),
                LocalDateTime.of(2026, 6, 10, 19, 30), price2);

        seedApprovedEvent(seller2, admin, venue2, "뮤지컬 위키드", "musical2",
                Category.MUSICAL, AgeLimit.AGE_12,
                LocalDate.of(2026, 9, 5), LocalDate.of(2026, 11, 30),
                LocalDateTime.of(2026, 9, 5, 19, 30), price2);

        seedApprovedEvent(seller1, admin, venue3, "반 고흐 인사이드 展", "exhibition1",
                Category.EXHIBITION, AgeLimit.ALL,
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 9, 30),
                LocalDateTime.of(2026, 5, 20, 11, 0), price3);

        seedApprovedEvent(seller2, admin, venue3, "한일전 축구 국가대표", "sports1",
                Category.SPORTS, AgeLimit.ALL,
                LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 22),
                LocalDateTime.of(2026, 6, 22, 20, 0), price3);

        // 4) PENDING 공연 (어드민 검수 화면에 노출)
        seedPendingEvent(seller1, "겨울왕국 라이브 인 콘서트", "kids1",
                Category.KIDS, AgeLimit.ALL,
                LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 31));

        seedPendingEvent(seller2, "야구 KBO 리그 - 두산 vs LG", "sports2",
                Category.SPORTS, AgeLimit.ALL,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 10));

        // 5) REJECTED 공연 (반려 사유 함께)
        seedRejectedEvent(seller2, admin, "OOO 의문의 콘서트", "concert3",
                Category.CONCERT, AgeLimit.ALL,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                "포스터 저작권 미확인");

        // 5-1) 다양한 운영 공연 ~90개 (카테고리·날짜 다양 + 회차 2~3)
        seedManyApprovedEvents(seller1, seller2, admin,
                List.of(venue1, venue2, venue3),
                List.of(price1, price2, price3));

        // 6) 예매 + 결제 + 리뷰 (normal 회원, 소량) / 7) 쿠폰
        seedReservationsAndReviews();
        seedCoupons();

        // 8) 성능 측정용 회원 1만 + 공연/좌석 풀 + 리뷰 (예매는 run()에서 JDBC batch로)
        PerfPool pool = seedPerfBaseData();

        // 9) 통계(과거 30일) / 10) 회원 변경 이력(정지)
        seedStatistics();
        seedMemberHistory();

        return pool;
    }

    // ===== 회원 =====

    private AdminMember seedAdmin() {
        return adminMemberRepository.save(AdminMember.create(
                "admin@test.com", passwordEncoder.encode(DEFAULT_PW),
                "관리자", "010-0000-0001",
                new Address("서울", "강남구 테헤란로 1", "06234"),
                "운영팀", "EMP001"
        ));
    }

    private Seller seedSeller(String email, String companyName, String repName, String bizNo) {
        return sellerRepository.save(Seller.create(
                email, passwordEncoder.encode(DEFAULT_PW),
                repName, "010-1111-1111",
                new Address("서울", "강남구 역삼동 123", "06234"),
                companyName, repName, bizNo
        ));
    }

    private void seedNormalMember() {
        normalMemberRepository.save(NormalMember.create(
                "normal@test.com", passwordEncoder.encode(DEFAULT_PW),
                "홍길동", "테스터123",
                LocalDate.of(1995, 5, 5),
                Gender.MALE,
                "010-2222-2222",
                new Address("서울", "마포구 와우산로 94", "04067")
        ));
    }

    // ===== 공연장 + 좌석 =====

    private Venue seedVenue(String name, int rows, int cols) {
        Venue venue = Venue.create(name, new Address("서울", name + " 주소", "00000"), rows, cols);
        venueRepository.save(venue);

        // 앞→뒤로 VIP / R / S / A 4등급 균등 분배
        List<SeatGradeRange> ranges = buildGradeRanges(rows);
        seatRepository.saveAll(venue.assignSeats(rows, cols, ranges));
        return venue;
    }

    // 행을 4구간으로 나눠 VIP/R/S/A 배정 (마지막 등급이 남는 행 흡수)
    private List<SeatGradeRange> buildGradeRanges(int rows) {
        SeatGrade[] grades = {SeatGrade.VIP, SeatGrade.R, SeatGrade.S, SeatGrade.A};
        List<SeatGradeRange> ranges = new ArrayList<>();
        int per = Math.max(1, rows / grades.length);
        int row = 1;
        for (int i = 0; i < grades.length; i++) {
            if (row > rows) break;
            int from = row;
            int to = (i == grades.length - 1) ? rows : Math.min(rows, row + per - 1);
            ranges.add(new SeatGradeRange(grades[i], from, to));
            row = to + 1;
        }
        return ranges;
    }

    // ===== 공연 + 회차 + 좌석 =====

    private void seedApprovedEvent(Seller seller, AdminMember admin, Venue venue,
                                   String title, String posterSeed,
                                   Category category, AgeLimit ageLimit,
                                   LocalDate startDate, LocalDate endDate,
                                   LocalDateTime showAt, Map<SeatGrade, Integer> priceMap) {
        Event event = createBaseEvent(seller, title, posterSeed, category, ageLimit, startDate, endDate);
        EventStatus prev = event.getStatus();
        event.approve();
        historyRepository.save(EventReviewHistory.of(
                event.getId(), ReviewAction.APPROVED, prev, event.getStatus(), null, admin.getId()
        ));
        seedSchedule(event, venue, showAt, priceMap);
    }

    private void seedPendingEvent(Seller seller, String title, String posterSeed,
                                  Category category, AgeLimit ageLimit,
                                  LocalDate startDate, LocalDate endDate) {
        createBaseEvent(seller, title, posterSeed, category, ageLimit, startDate, endDate);
        // 상태 그대로 PENDING (Event.create()가 PENDING으로 시작)
    }

    private void seedRejectedEvent(Seller seller, AdminMember admin,
                                   String title, String posterSeed,
                                   Category category, AgeLimit ageLimit,
                                   LocalDate startDate, LocalDate endDate,
                                   String reason) {
        Event event = createBaseEvent(seller, title, posterSeed, category, ageLimit, startDate, endDate);
        EventStatus prev = event.getStatus();
        event.reject();
        historyRepository.save(EventReviewHistory.of(
                event.getId(), ReviewAction.REJECTED, prev, event.getStatus(), reason, admin.getId()
        ));
    }

    private Event createBaseEvent(Seller seller, String title, String posterSeed,
                                  Category category, AgeLimit ageLimit,
                                  LocalDate startDate, LocalDate endDate) {
        return eventRepository.save(Event.create(
                title,
                title + " 공연 소개입니다. 자세한 정보는 공연 페이지를 확인해주세요.",
                startDate, endDate,
                120,
                "출연진 미정",
                ageLimit,
                category,
                "https://picsum.photos/seed/" + posterSeed + "/400/600",
                seller
        ));
    }

    /** 회차 + 좌석 매핑 (EventScheduleService.create 와 동일 로직) */
    private void seedSchedule(Event event, Venue venue, LocalDateTime showAt,
                              Map<SeatGrade, Integer> priceMap) {
        int nextRound = eventScheduleRepository.countByEventIdAndVenueId(event.getId(), venue.getId()) + 1;
        EventSchedule schedule = EventSchedule.create(venue, showAt, nextRound);
        event.addSchedule(schedule);
        eventScheduleRepository.save(schedule);

        List<Seat> seats = seatRepository.findByVenueId(venue.getId());
        List<EventSeat> eventSeats = seats.stream()
                .map(seat -> EventSeat.create(schedule, seat, priceMap.get(seat.getSeatGrade())))
                .toList();
        eventSeats.forEach(schedule::addEventSeat);
        eventSeatRepository.saveAll(eventSeats);
    }

    // ===== 예매 + 결제 + 리뷰 (normal 회원, 소량) =====
    private void seedReservationsAndReviews() {
        Member normal = memberRepository.findByEmail("normal@test.com").orElseThrow();
        List<Event> events = eventRepository.findByStatus(EventStatus.APPROVED);

        int count = 0;
        for (Event event : events) {
            if (count >= 3) break;
            if (event.getSchedules().isEmpty()) continue;

            EventSchedule schedule = event.getSchedules().get(0);
            List<EventSeat> seats = eventSeatRepository.findByEventScheduleId(schedule.getId());
            if (seats.isEmpty()) continue;
            EventSeat seat = seats.get(0);

            // 예매(PENDING) → 결제(CONFIRMED)
            Reservation reservation = Reservation.create(
                    normal, schedule, "seed-res-" + event.getId(), seat.getPrice());
            reservation.addReservationSeat(ReservationSeat.create(seat, seat.getPrice()));
            seat.reserve();
            reservationRepository.save(reservation);
            reservationHistoryRepository.save(ReservationHistory.of(reservation));

            Payment payment = Payment.paid(reservation, reservation.getTotalPrice());
            paymentRepository.save(payment);
            paymentHistoryRepository.save(PaymentHistory.of(payment, null));
            reservation.confirm();
            reservationHistoryRepository.save(ReservationHistory.of(reservation));

            // 리뷰
            reviewRepository.save(Review.create(event, normal, 5 - (count % 2),
                    event.getTitle() + " 정말 좋았습니다. 강력 추천!"));

            count++;
        }
    }

    // ===== 쿠폰 (선착순 — Redis 재고 세팅) =====
    private void seedCoupons() {
        seedCoupon("신규가입 축하 5,000원 할인", DiscountType.FIXED, 5000, 1000);
        seedCoupon("주말 특가 20% 할인", DiscountType.RATE, 20, 500);
        seedCoupon("선착순 10,000원 할인", DiscountType.FIXED, 10000, 100);
    }

    private void seedCoupon(String name, DiscountType type, int value, int quantity) {
        Coupon coupon = couponRepository.save(Coupon.create(name, type, value, quantity));
        redisTemplate.opsForValue().set("coupon:stock:" + coupon.getId(), String.valueOf(quantity));
    }

    // ===== 성능 측정용 대량 시드 =====
    private PerfPool seedPerfBaseData() {
        // 비밀번호는 한 번만 인코딩 (bcrypt 1만번이면 느려서 재사용)
        String pw = passwordEncoder.encode(DEFAULT_PW);

        // 1) 회원 1만 (memberIds.get(0) = 헤비유저)
        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < PERF_MEMBERS; i++) {
            NormalMember m = NormalMember.create(
                    "perf" + i + "@test.com", pw, "유저" + i, "퍼프닉" + i,
                    LocalDate.of(1995, 1, 1), Gender.MALE, "010-0000-0000",
                    new Address("서울", "테스트로 " + i, "00000"));
            normalMemberRepository.save(m);
            memberIds.add(m.getId());
            if (i % 1000 == 0) { em.flush(); em.clear(); }
        }
        em.flush();
        em.clear();

        // 2) 대형 공연 1개 + 좌석 1000 (event_seat) — reservation 10만이 공유 참조
        Seller seller = (Seller) memberRepository.findByEmail("seller1@test.com").orElseThrow();
        Venue venue = venueRepository.save(Venue.create(
                "성능테스트홀", new Address("서울", "성능로 1", "00000"), 50, 20));
        seatRepository.saveAll(venue.assignSeats(50, 20, buildGradeRanges(50)));

        Event event = eventRepository.save(Event.create(
                "성능 측정용 공연", "부하테스트용 대량 데이터",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                120, "출연진", AgeLimit.ALL, Category.CONCERT,
                "https://picsum.photos/seed/perf/400/600", seller));
        event.approve();

        EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(10), 1);
        event.addSchedule(schedule);
        eventScheduleRepository.save(schedule);

        List<Seat> seats = seatRepository.findByVenueId(venue.getId());
        List<EventSeat> eventSeats = seats.stream()
                .map(s -> EventSeat.create(schedule, s, 100000))
                .toList();
        eventSeatRepository.saveAll(eventSeats);

        em.flush();   // 성능 공연 좌석 DB 반영 (아래 풀 조회 위해)

        // === 모든 APPROVED 공연 좌석을 풀로 수집 → reservation이 여러 공연/회차에 고르게 분산 ===
        // clear 후에도 쓰도록 ID·가격을 값으로 추출
        List<Long> poolScheduleIds = new ArrayList<>();
        List<Long> poolSeatIds = new ArrayList<>();
        List<Integer> poolPrices = new ArrayList<>();
        for (Event e : eventRepository.findByStatus(EventStatus.APPROVED)) {
            for (EventSchedule sch : e.getSchedules()) {
                for (EventSeat es : eventSeatRepository.findByEventScheduleId(sch.getId())) {
                    poolScheduleIds.add(sch.getId());
                    poolSeatIds.add(es.getId());
                    poolPrices.add(es.getPrice());
                }
            }
        }
        em.flush();
        em.clear();

        // 3) 리뷰 — 각 APPROVED 공연에 REVIEWS_PER_EVENT명 (평점순 랭킹용, 최소 10개+)
        List<Long> eventIds = eventRepository.findByStatus(EventStatus.APPROVED)
                .stream().map(Event::getId).toList();
        for (Long eventId : eventIds) {
            Event ev = em.getReference(Event.class, eventId);
            for (int j = 0; j < REVIEWS_PER_EVENT; j++) {
                Member m = em.getReference(NormalMember.class, memberIds.get(j));
                reviewRepository.save(Review.create(ev, m, 3 + (j % 3), "성능 시드 리뷰 " + j));
            }
            em.flush();
            em.clear();
        }

        return new PerfPool(memberIds.get(0), memberIds, poolScheduleIds, poolSeatIds, poolPrices);
    }

    // 예매·결제 대량 적재 = JDBC batch + 청크 커밋.
    // - IDENTITY라 JPA는 batch가 안 묶이고, 단일 트랜잭션은 미커밋 누적으로 메모리 폭발 → JDBC batch로 우회.
    // - reservation은 id를 명시(자식 reservation_seat·payment가 FK로 참조). 자식 id는 auto_increment에 맡김.
    private void seedPerfReservationsJdbc(TransactionTemplate tx, PerfPool pool) {
        long baseId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM reservation", Long.class);
        LocalDateTime now = LocalDateTime.now();
        Random rnd = new Random();
        int chunkSize = 10000;

        for (int start = 0; start < PERF_RESERVATIONS; start += chunkSize) {
            int end = Math.min(start + chunkSize, PERF_RESERVATIONS);

            List<Object[]> reservationArgs = new ArrayList<>();
            List<Object[]> seatArgs = new ArrayList<>();
            List<Object[]> paymentArgs = new ArrayList<>();

            for (int i = start; i < end; i++) {
                long reservationId = baseId + 1 + i;
                Long memberId = (i < PERF_HEAVY_RESERVATIONS)
                        ? pool.heavyId() : pool.memberIds().get(rnd.nextInt(pool.memberIds().size()));
                int s = rnd.nextInt(pool.seatIds().size());   // 풀에서 랜덤 → 공연/회차 고르게 분산
                Long scheduleId = pool.scheduleIds().get(s);
                Long eventSeatId = pool.seatIds().get(s);
                int price = pool.prices().get(s);

                reservationArgs.add(new Object[]{reservationId, memberId, scheduleId, "perf-res-" + i, price, "CONFIRMED", now, now, "system"});
                seatArgs.add(new Object[]{reservationId, eventSeatId, price, now, now, "system"});
                paymentArgs.add(new Object[]{reservationId, price, "PAID", "MOCK", now, now, "system"});
            }

            // 청크마다 별도 트랜잭션으로 커밋 → undo log가 쌓이지 않아 메모리 안전
            tx.executeWithoutResult(status -> {
                jdbcTemplate.batchUpdate(
                        "INSERT INTO reservation (id, member_id, schedule_id, idempotency_key, total_price, status, created_at, updated_at, created_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", reservationArgs);
                jdbcTemplate.batchUpdate(
                        "INSERT INTO reservation_seat (reservation_id, event_seat_id, price, created_at, updated_at, created_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?)", seatArgs);
                jdbcTemplate.batchUpdate(
                        "INSERT INTO payment (reservation_id, amount, status, method, created_at, updated_at, created_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)", paymentArgs);
            });

            log.info("[Seed] 예매 적재 {}/{}", end, PERF_RESERVATIONS);
        }
    }

    private record PerfPool(Long heavyId, List<Long> memberIds, List<Long> scheduleIds,
                            List<Long> seatIds, List<Integer> prices) {
    }

    // ===== 다양한 운영 공연 ~90개 (카테고리·제목·날짜·회차 다양) =====
    private void seedManyApprovedEvents(Seller s1, Seller s2, AdminMember admin,
                                        List<Venue> venues, List<Map<SeatGrade, Integer>> priceMaps) {
        Map<Category, String[]> pool = titlePool();
        Category[] cats = Category.values();
        AgeLimit[] ages = AgeLimit.values();
        Random rnd = new Random();
        LocalDate base = LocalDate.now();

        for (int i = 0; i < 90; i++) {
            Category cat = cats[rnd.nextInt(cats.length)];
            String[] titles = pool.get(cat);
            String title = titles[rnd.nextInt(titles.length)] + " " + (i + 1) + "기";

            int v = rnd.nextInt(venues.size());
            Venue venue = venues.get(v);
            Map<SeatGrade, Integer> priceMap = priceMaps.get(v);
            Seller seller = (i % 2 == 0) ? s1 : s2;
            AgeLimit age = ages[rnd.nextInt(ages.length)];

            LocalDate start = base.plusDays(rnd.nextInt(240) - 60);   // 오늘 -60 ~ +180
            LocalDate end = start.plusDays(7 + rnd.nextInt(60));

            Event event = createBaseEvent(seller, title, "seed-many-" + i, cat, age, start, end);
            EventStatus prev = event.getStatus();
            event.approve();
            historyRepository.save(EventReviewHistory.of(
                    event.getId(), ReviewAction.APPROVED, prev, event.getStatus(), null, admin.getId()));

            // 회차 2~3개 (날짜 다양, roundNumber 자동 1·2·3)
            int rounds = 2 + rnd.nextInt(2);
            for (int r = 0; r < rounds; r++) {
                LocalDateTime showAt = start.plusDays((long) r * 7).atTime(19, 30);
                seedSchedule(event, venue, showAt, priceMap);
            }
        }
    }

    private Map<Category, String[]> titlePool() {
        return Map.of(
                Category.CONCERT, new String[]{"아이유 콘서트", "세븐틴 팬미팅", "뉴진스 쇼케이스", "아이브 단독공연", "에스파 라이브"},
                Category.MUSICAL, new String[]{"레미제라블", "위키드", "오페라의 유령", "노트르담 드 파리", "지킬앤하이드"},
                Category.SPORTS, new String[]{"프로야구 올스타전", "프로농구 챔피언전", "K리그 결승", "프로배구 V리그"},
                Category.EXHIBITION, new String[]{"반 고흐 인사이드", "모네 특별전", "팀랩 보더리스", "클림트 전시"},
                Category.KIDS, new String[]{"겨울왕국 라이브", "뽀로로 뮤지컬", "핑크퐁 콘서트", "콩순이 쇼"});
    }

    // ===== 통계 (과거 30일 일별 매출/예매수 — 오늘은 실시간 집계라 제외) =====
    private void seedStatistics() {
        LocalDate today = LocalDate.now();
        List<Long> eventIds = eventRepository.findByStatus(EventStatus.APPROVED)
                .stream().map(Event::getId).toList();
        Random rnd = new Random();

        for (int d = 1; d <= 30; d++) {
            LocalDate date = today.minusDays(d);
            long totalOrders = 0;
            for (Long eventId : eventIds) {
                long cnt = 5 + rnd.nextInt(45);   // 공연별 5~49건
                dailyEventStatsRepository.save(DailyEventStats.of(date, eventId, cnt));
                totalOrders += cnt;
            }
            dailySalesStatsRepository.save(DailySalesStats.of(date, totalOrders, totalOrders * 100000L));
        }
    }

    // ===== 회원 변경 이력 (perf 회원 5명 정지 — 헤비유저 perf0은 제외) =====
    private void seedMemberHistory() {
        for (int i = 1; i <= 5; i++) {
            Member m = memberRepository.findByEmail("perf" + i + "@test.com").orElseThrow();
            MemberStatus prev = m.getMemberStatus();
            m.suspend();
            memberHistoryRepository.save(
                    MemberHistory.of(m.getId(), prev, m.getMemberStatus(), "결제 어뷰징 신고 누적 (시드)"));
        }
    }
}
