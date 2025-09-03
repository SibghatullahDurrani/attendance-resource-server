package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.user.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_generator")
    @SequenceGenerator(name = "user_id_generator", sequenceName = "user_id_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    private String profilePictureName;

    private String sourceFacePictureName;

    @Column(name = "second_name", nullable = false, length = 30)
    private String secondName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "role", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private String identificationNumber;

    private String phoneNumber;

    private String email;

    private String designation;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "user")
    private List<Attendance> attendances;

    @Column(nullable = false)
    private int remainingSickLeaves;

    @Column(nullable = false)
    private int remainingAnnualLeaves;

    @OneToMany(mappedBy = "user")
    private List<Leave> leaves;

    @OneToMany(mappedBy = "user")
    private List<UserNotification> userNotifications;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift userShift;

    @OneToOne
    @JoinColumn(name = "user_shift_setting_id", nullable = false)
    private UserShiftSetting userShiftSetting;

    @Column(nullable = false)
    private boolean isSavedInProducer;

    private Date lastSavedInProducerDate;

}
