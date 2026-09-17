package com.ticketing.outbox.repository;

import com.ticketing.outbox.domain.OutboxEvent;
import com.ticketing.outbox.domain.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    Optional<OutboxEvent> findByMessageId(String messageId);

    long countByStatus(OutboxEventStatus status);

    List<OutboxEvent> findByStatusOrderByIdAsc(OutboxEventStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update OutboxEvent o
               set o.status = com.ticketing.outbox.domain.OutboxEventStatus.PROCESSING,
                   o.claimedBy = :workerId,
                   o.claimedAt = :claimedAt
             where o.id = :id
               and o.status = com.ticketing.outbox.domain.OutboxEventStatus.PENDING
            """)
    int tryClaim(@Param("id") Long id,
                 @Param("workerId") String workerId,
                 @Param("claimedAt") LocalDateTime claimedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update OutboxEvent o
               set o.status = com.ticketing.outbox.domain.OutboxEventStatus.PUBLISHED,
                   o.claimedBy = null,
                   o.claimedAt = null
             where o.id = :id
               and o.status = com.ticketing.outbox.domain.OutboxEventStatus.PROCESSING
               and o.claimedBy = :workerId
            """)
    int markPublished(@Param("id") Long id, @Param("workerId") String workerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update OutboxEvent o
               set o.status = com.ticketing.outbox.domain.OutboxEventStatus.PENDING,
                   o.claimedBy = null,
                   o.claimedAt = null
             where o.id = :id
               and o.status = com.ticketing.outbox.domain.OutboxEventStatus.PROCESSING
               and o.claimedBy = :workerId
            """)
    int releaseClaim(@Param("id") Long id, @Param("workerId") String workerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update OutboxEvent o
               set o.status = com.ticketing.outbox.domain.OutboxEventStatus.PENDING,
                   o.claimedBy = null,
                   o.claimedAt = null
             where o.status = com.ticketing.outbox.domain.OutboxEventStatus.PROCESSING
               and o.claimedAt < :expiredBefore
            """)
    int releaseExpiredClaims(@Param("expiredBefore") LocalDateTime expiredBefore);
}
