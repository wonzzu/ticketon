package com.ticketing.notification.repository;

import com.ticketing.notification.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByMemberIdOrderByIdDesc(
            Long memberId,
            Pageable pageable
    );

    Slice<Notification> findByMemberIdAndIdLessThanOrderByIdDesc(
            Long memberId,
            Long cursor,
            Pageable pageable
    );

    long countByMemberIdAndReadAtIsNull(Long memberId);

    Optional<Notification> findByIdAndMemberId(
            Long notificationId,
            Long memberId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification n
               SET n.readAt = :readAt
             WHERE n.memberId = :memberId
               AND n.readAt IS NULL
            """)
    int markAllAsRead(
            @Param("memberId") Long memberId,
            @Param("readAt") LocalDateTime readAt
    );
}
