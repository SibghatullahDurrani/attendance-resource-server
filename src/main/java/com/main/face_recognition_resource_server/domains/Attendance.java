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

}
