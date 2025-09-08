package com.codegym.socialmedia.model.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_records")
public class TrackingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;

    private String endpoint;

    private LocalDateTime timestamp;

    private String username;
}

