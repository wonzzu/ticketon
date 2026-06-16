package com.ticketing.review.repository;

import com.ticketing.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    Page<Review> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

    Page<Review> findByEventIdOrderByRatingDesc(Long eventId,Pageable pageable);

    List<Review> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    @Query("select avg(r.rating) from Review r where r.event.id = :eventId")
    Double findAverageRating(Long eventId);

    long countByEventId(Long eventId);


}
