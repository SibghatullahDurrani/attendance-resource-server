package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.LeaveStatus;
import com.main.face_recognition_resource_server.constants.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "leaves")
public class Leave {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leave_id_generator")
  @SequenceGenerator(name = "leave_id_generator", sequenceName = "leave_id_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private Date date;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String leaveApplication;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private LeaveStatus status;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private LeaveType type;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;
}
