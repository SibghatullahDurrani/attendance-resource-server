package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.repositories.CameraRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CameraServicesImplTest {
  @Mock
  private CameraRepository cameraRepository;

  @InjectMocks
  private CameraServicesImpl cameraServices;

}