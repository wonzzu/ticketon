package com.ticketing.review.repository;

import com.ticketing.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventIdOrderByCreatedAtDesc(Long eventId);

    List<Review> findByEventIdOrderByRatingDesc(Long eventId);

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    @Query("select avg(r.rating) from Review r where r.event.id = :eventId")
    Double findAverageRating(Long eventId);

    long countByEventId(Long eventId);


}
