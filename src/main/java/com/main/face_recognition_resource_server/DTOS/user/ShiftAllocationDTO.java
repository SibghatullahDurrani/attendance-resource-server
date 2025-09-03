package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.shift.ShiftMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftAllocationDTO {
    private Long userId;
    private String fullName;
    private String designation;
    private String department;
    private String shiftName;
    private ShiftMode shiftMode;
    private Long startDate;
    private Long endDate;

    public ShiftAllocationDTO(Long userId, String firstName, String secondName, String designation, String department, String shiftName, ShiftMode shiftMode, Date startDate, Date endDate) {
        this.userId = userId;
        this.fullName = firstName + " " + secondName;
        this.designation = designation;
        this.department = department;
        this.shiftName = shiftName;
        this.shiftMode = shiftMode;
        if (startDate == null) {
            this.startDate = 0L;
        } else {
            this.startDate = startDate.getTime();
        }
        if (endDate == null) {
            this.endDate = 0L;
        } else {
            this.endDate = endDate.getTime();
        }
    }
}
