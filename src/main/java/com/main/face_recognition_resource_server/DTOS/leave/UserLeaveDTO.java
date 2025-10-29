package com.main.face_recognition_resource_server.DTOS.leave;

import com.main.face_recognition_resource_server.constants.leave.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserLeaveDTO {
    private long id;
    private long date;
    private LeaveStatus status;

    public UserLeaveDTO(long id, Date date, LeaveStatus status) {
        this.id = id;
        this.date = date.getTime();
        this.status = status;
    }
}
