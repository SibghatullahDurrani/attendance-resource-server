package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendances")
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendance_id_generator")
  @SequenceGenerator(name = "attendance_id_generator", sequenceName = "attendance_id_sequence", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Date date;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttendanceStatus status;

  @OneToMany(mappedBy = "attendance")
  private List<CheckIn> checkIns;

  @OneToMany(mappedBy = "attendance")
  private List<CheckOut> checkOuts;
}
