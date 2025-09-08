package com.codegym.socialmedia.model.admin;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "report_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Ví dụ: "SPAM", "ABUSE", "HARASSMENT", ...

    @Column(columnDefinition = "TEXT")
    private String description;

//    @OneToMany(mappedBy = "reportType")
//    private List<Report> reports;

    @OneToMany(mappedBy = "reportType")
    private List<ModerationLog> reports;

}
