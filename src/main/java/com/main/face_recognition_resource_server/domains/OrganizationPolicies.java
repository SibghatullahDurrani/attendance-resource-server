package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "organization_policies")
public class OrganizationPolicies {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_policies_id_generator")
  @SequenceGenerator(name = "organization_policies_id_generator", sequenceName = "organization_policies_id_sequence", allocationSize = 1)
  private Long id;

  private String checkInTimeForUser;

  private String checkOutTimeForUser;

  private int lateAttendanceToleranceTimeInMinutes;

  private int retakeAttendanceInHour;

  private int checkOutToleranceTimeInHour;

  @Column(nullable = false)
  private int sickLeavesAllowed;

  @Column(nullable = false)
  private int annualLeavesAllowed;
}
