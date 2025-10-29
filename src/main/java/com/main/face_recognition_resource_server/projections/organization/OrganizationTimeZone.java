package com.main.face_recognition_resource_server.projections.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrganizationTimeZone {
    private Long organizationId;
    private String timeZone;
}
