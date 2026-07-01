package com.ticketing.event.service;

import com.ticketing.admin.service.AdminEventService;
import com.ticketing.event.domain.AgeLimit;
import com.ticketing.event.domain.Category;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventSchedule;
import com.ticketing.event.dto.response.EventListResponseDto;
import com.ticketing.global.entity.Address;
import com.ticketing.member.domain.AdminMember;
import com.ticketing.member.domain.Seller;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 성능(캐시 무효화) — 목록(events) 캐시가 켜진 뒤, 공연 승인(@CacheEvict) 시
 * 캐시가 무효화되어 승인된 공연이 목록에 즉시 반영되는지 검증.
 * evict를 빠뜨리면 재조회가 옛 캐시(승인 공연 없음)를 반환해 실패한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)   // DB 정리
@DisplayName("성능 - 공연 목록 캐시 무효화")
class EventCacheEvictTest {

    @Autowired EventService eventService;
    @Autowired AdminEventService adminEventService;
    @Autowired CacheManager cacheManager;
    @Autowired TransactionTemplate tx;
    @PersistenceContext EntityManager em;

    private Long pendingEventId;
    private Long adminId;

    @BeforeEach
    void setup() {
        tx.execute(s -> {
            AdminMember admin = AdminMember.create("admin@t.com", "pw", "관리자", "010-0000-0000",
                    new Address("서울", "로1", "00000"), "운영팀", "EMP-001");
            em.persist(admin);
            adminId = admin.getId();

            Seller seller = Seller.create("evict-seller@t.com", "pw", "셀러", "010-0000-0001",
                    new Address("서울", "로2", "00000"), "컴퍼니", "대표", "000-00-00001");
            em.persist(seller);

            Venue venue = Venue.create("무효화홀", new Address("서울", "로3", "00000"), 1, 1);
            em.persist(venue);

            // PENDING 공연 + 회차 (approve 후 APPROVED가 되면 목록 조건 충족: APPROVED + 회차 존재)
            Event event = Event.create("승인대기공연", "설명", LocalDate.now(), LocalDate.now().plusDays(1),
                    120, "출연", AgeLimit.ALL, Category.CONCERT, "url", seller);
            EventSchedule schedule = EventSchedule.create(venue, LocalDateTime.now().plusDays(1), 1);
            event.addSchedule(schedule);
            em.persist(event);
            em.persist(schedule);
            pendingEventId = event.getId();

            em.flush();
            return null;
        });
    }

    @AfterEach
    void clearCache() {   // 캐시 격리 (Redis에 남은 캐시 제거)
        cacheManager.getCacheNames().forEach(n -> {
            var c = cacheManager.getCache(n);
            if (c != null) c.clear();
        });
    }

    @Test
    @DisplayName("공연 승인 시 목록 캐시 무효화 → 승인된 공연이 목록에 반영")
    void 승인시_목록캐시_무효화() {
        Cache eventsCache = cacheManager.getCache("events");

        // given : 조회로 목록 캐시 생성 (승인 전이라 목록엔 아직 없음)
        List<EventListResponseDto> before = eventService.search(null, null);
        assertThat(before).noneMatch(e -> e.getId().equals(pendingEventId));
        assertThat(eventsCache.get("all")).isNotNull();          // 캐시 생성 확인 (Miss→적재)

        // when : 승인 → @CacheEvict(events, 'all')
        adminEventService.approve(pendingEventId, adminId);
        assertThat(eventsCache.get("all")).isNull();             // evict 직접 확인 (캐시 비워짐)

        // then : 재조회 시 새 캐시 + 승인 공연 반영
        List<EventListResponseDto> after = eventService.search(null, null);
        assertThat(after).anyMatch(e -> e.getId().equals(pendingEventId));
        assertThat(eventsCache.get("all")).isNotNull();          // 재적재 확인
    }
}
