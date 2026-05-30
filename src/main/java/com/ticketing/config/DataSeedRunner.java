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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional
    public void run(String... args) {
        if (memberRepository.existsByEmail("admin@test.com")) {
            log.info("[Seed] 이미 시드 데이터 존재 → 건너뜀");
            return;
        }

        log.info("[Seed] 더미 데이터 생성 시작");

        // 1) 회원
        AdminMember admin = seedAdmin();
        Seller seller1 = seedSeller("seller1@test.com", "엔터테인먼트A", "김셀러", "111-11-11111");
        Seller seller2 = seedSeller("seller2@test.com", "프로덕션B", "박셀러", "222-22-22222");
        seedNormalMember();

        // 2) 공연장 + 좌석 (등급: R / S 2등급)
        Venue venue1 = seedVenue("잠실 종합운동장", 5, 5);
        Venue venue2 = seedVenue("블루스퀘어 신한카드홀", 4, 6);
        Venue venue3 = seedVenue("예술의전당 콘서트홀", 5, 6);

        Map<SeatGrade, Integer> price1 = Map.of(SeatGrade.R, 165000, SeatGrade.S, 121000);
        Map<SeatGrade, Integer> price2 = Map.of(SeatGrade.R, 132000, SeatGrade.S,  99000);
        Map<SeatGrade, Integer> price3 = Map.of(SeatGrade.R,  88000, SeatGrade.S,  66000);

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

        log.info("[Seed] 더미 데이터 생성 완료");
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

        // 앞쪽 절반 R석, 나머지 S석
        int mid = (rows + 1) / 2;
        List<SeatGradeRange> ranges = List.of(
                new SeatGradeRange(SeatGrade.R, 1, mid),
                new SeatGradeRange(SeatGrade.S, mid + 1, rows)
        );
        seatRepository.saveAll(venue.assignSeats(rows, cols, ranges));
        return venue;
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
                event, ReviewAction.APPROVED, prev, event.getStatus(), null, admin
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
                event, ReviewAction.REJECTED, prev, event.getStatus(), reason, admin
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
}
