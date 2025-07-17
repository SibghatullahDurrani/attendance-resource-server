package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Table(name = "check_outs")
public class CheckOut {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checkout_id_generator")
  @SequenceGenerator(name = "checkout_id_generator", sequenceName = "checkout_id_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private Date date;

  @ManyToOne
  @JoinColumn(name = "attendance_id", nullable = false)
  private Attendance attendance;

  private String fullImageName;

  private String faceImageName;
}
