package com.main.face_recognition_resource_server.services.usershiftsetting;

import com.main.face_recognition_resource_server.constants.shift.ShiftMode;
import com.main.face_recognition_resource_server.domains.UserShiftSetting;
import com.main.face_recognition_resource_server.repositories.UserShiftRepository;
import org.springframework.stereotype.Service;

@Service
public class UserShiftSettingServiceImpl implements UserShiftSettingService {
    private final UserShiftRepository userShiftRepository;

    public UserShiftSettingServiceImpl(UserShiftRepository userShiftRepository) {
        this.userShiftRepository = userShiftRepository;
    }

    @Override
    public UserShiftSetting registerUserShiftSetting() {
        return userShiftRepository.save(UserShiftSetting.builder()
                .shiftMode(ShiftMode.PERMANENT)
                .build());
    }
}
