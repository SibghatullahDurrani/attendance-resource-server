package com.main.face_recognition_resource_server.DTOS.leave;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeavesAllowedPolicyDTO {
  private int sickLeavesAllowed;
  private int annualLeavesAllowed;
}
