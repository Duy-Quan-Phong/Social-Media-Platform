package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.admin.TrackingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface TrackingRepository extends JpaRepository<TrackingRecord,Long> {
    @Query("SELECT COUNT(t) FROM TrackingRecord t WHERE DATE(t.timestamp) = :date")
    long countVisitsOn(LocalDate date);

    @Query("SELECT COUNT(t) FROM TrackingRecord t WHERE DATE(t.timestamp) >= :from")
    long countVisitsFrom(LocalDate from);
}
