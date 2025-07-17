package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_id_generator")
  @SequenceGenerator(name = "notification_id_generator", sequenceName = "notification_id_sequence", allocationSize = 1)
  private Long id;

  private String title;

  @Column(nullable = false)
  private String message;

  @OneToOne()
  @JoinColumn(name = "attachment_id", nullable = true)
  private Attachment attachment;

  @OneToMany(mappedBy = "notification")
  private List<UserNotification> userNotifications;

}
