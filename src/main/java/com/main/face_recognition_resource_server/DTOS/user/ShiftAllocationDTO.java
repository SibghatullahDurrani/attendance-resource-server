package com.main.face_recognition_resource_server.DTOS.user;

import com.main.face_recognition_resource_server.constants.ShiftMode;
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
    private Long id;
    private String fullName;
    private String designation;
    private String department;
    private String shiftName;
    private ShiftMode shiftMode;
    private Long from;
    private Long to;

    public ShiftAllocationDTO(Long id, String firstName, String secondName, String designation, String department, String shiftName, ShiftMode shiftMode, Date from, Date to) {
        this.id = id;
        this.fullName = firstName + " " + secondName;
        this.designation = designation;
        this.department = department;
        this.shiftName = shiftName;
        this.shiftMode = shiftMode;
        if (from == null) {
            this.from = 0L;
        } else {
            this.from = from.getTime();
        }
        if (to == null) {
            this.to = 0L;
        } else {
            this.to = to.getTime();
        }
    }
}
