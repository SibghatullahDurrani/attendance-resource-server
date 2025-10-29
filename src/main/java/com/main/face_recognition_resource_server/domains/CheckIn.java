package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "check_ins")
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checkin_id_generator")
    @SequenceGenerator(name = "checkin_id_generator", sequenceName = "checkin_id_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, columnDefinition = "timestamptz")
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    private String fullImageName;

    private String faceImageName;
}
