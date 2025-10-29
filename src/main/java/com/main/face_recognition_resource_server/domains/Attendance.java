package com.main.face_recognition_resource_server.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceStatus;
import com.main.face_recognition_resource_server.constants.attendance.AttendanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendance_id_generator")
    @SequenceGenerator(name = "attendance_id_generator", sequenceName = "attendance_id_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "timestamptz")
    private Instant date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @OneToMany(mappedBy = "attendance", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CheckIn> checkIns;

    @OneToMany(mappedBy = "attendance", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CheckOut> checkOuts;

    @Enumerated(EnumType.STRING)
    private AttendanceType currentAttendanceStatus;
}
