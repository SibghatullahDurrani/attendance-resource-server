package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "check_ins")
public class CheckIn {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checkin_id_generator")
  @SequenceGenerator(name = "checkin_id_generator", sequenceName = "checkin_id_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private Date date;

  @ManyToOne
  @JoinColumn(name = "attendance_id", nullable = false)
  private Attendance attendance;

  @Column(nullable = false)
  private String fullImageName;

  @Column(nullable = false)
  private String faceImageName;
}
