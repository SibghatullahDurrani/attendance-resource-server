package com.main.face_recognition_resource_server.services.camera;

import com.main.face_recognition_resource_server.DTOS.camera.GetCameraDTO;
import com.main.face_recognition_resource_server.DTOS.camera.RegisterCameraDTO;
import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.domains.Camera;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CameraServicesImpl implements CameraServices {
  private final CameraRepository cameraRepository;

  public CameraServicesImpl(CameraRepository cameraRepository) {
    this.cameraRepository = cameraRepository;
  }

  @Override
  public void registerCamera(RegisterCameraDTO cameraToRegister, Organization organization) throws CameraAlreadyExistsInOrganizationException {
    boolean cameraExists = cameraExists(cameraToRegister.getIpAddress(), cameraToRegister.getPort(), cameraToRegister.getChannel());
    if (!cameraExists) {
      Camera camera = Camera.builder()
              .ipAddress(cameraToRegister.getIpAddress())
              .port(cameraToRegister.getPort())
              .channel(cameraToRegister.getChannel())
              .username(cameraToRegister.getUsername())
              .password(cameraToRegister.getPassword())
              .type(cameraToRegister.getType())
              .cameraStatus(CameraStatus.INACTIVE)
              .organization(organization)
              .build();

      cameraRepository.saveAndFlush(camera);
    }
  }


  @Override
  public List<GetCameraDTO> getCamerasOfOrganization(Long organizationId) {
    return cameraRepository.getCamerasOfOrganization(organizationId);
  }

  @Override
  public boolean cameraExists(String ipAddress, int port, int channel) throws CameraAlreadyExistsInOrganizationException {
    boolean exists = cameraRepository.existsByIpAddressAndPortAndChannel(ipAddress, port, channel);
    if (exists) {
      throw new CameraAlreadyExistsInOrganizationException();
    } else {
      return false;
    }
  }
}
