package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.ShiftMode;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "user_shift_settings")
@ToString
public class UserShiftSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_shift_settings_id_generator")
    @SequenceGenerator(name = "user_shift_settings_id_generator", sequenceName = "user_shift_settings_id_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShiftMode shiftMode;

    private Date startDate;

    private Date endDate;
}
