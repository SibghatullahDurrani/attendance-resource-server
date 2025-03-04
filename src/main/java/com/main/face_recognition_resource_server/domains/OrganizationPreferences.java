package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "organization_preferences")
public class OrganizationPreferences {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_preferences_id_generator")
  @SequenceGenerator(name = "organization_preferences_generator", sequenceName = "organization_preferences_id_sequence", allocationSize = 1)
  private Long id;

  private Date checkInTimeForUser;

  private Date checkOutTimeForUser;

  private int minutesTillAttendanceCountsAsLate;

  private int retakeAttendanceInHour;

  private int checkOutToleranceTimeInHour;
}
