package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.camera.CameraCredentialsDTO;
import com.main.face_recognition_resource_server.DTOS.camera.GetCameraDTO;
import com.main.face_recognition_resource_server.DTOS.camera.RegisterCameraDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;
import com.main.face_recognition_resource_server.exceptions.NoInCameraExistsException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.repositories.CameraRepository;
import com.main.face_recognition_resource_server.services.camera.CameraServices;
import com.main.face_recognition_resource_server.services.camera.CameraSubscriptionServices;
import com.main.face_recognition_resource_server.services.camera.dahua.DahuaLogin;
import com.main.face_recognition_resource_server.services.camera.dahua.SDKInstance;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.netsdk.lib.NetSDKLib.NET_GROUPID_LENGTH;
import static com.netsdk.lib.NetSDKLib.NET_GROUPNAME_LENGTH;

@Slf4j
@RestController
@RequestMapping("cameras")
public class CameraController {
  private final CameraServices cameraServices;
  private final OrganizationServices organizationServices;
  private final CameraSubscriptionServices cameraSubscriptionServices;
  private final CameraRepository cameraRepository;
  private final NetSDKLib sdkInstance;

  public CameraController(CameraServices cameraServices, OrganizationServices organizationServices, CameraSubscriptionServices cameraSubscriptionServices, CameraRepository cameraRepository) {
    this.cameraServices = cameraServices;
    this.organizationServices = organizationServices;
    this.cameraSubscriptionServices = cameraSubscriptionServices;
    this.cameraRepository = cameraRepository;
    this.sdkInstance = SDKInstance.getInstance();
  }

  @GetMapping("test")
  public void test() throws IOException {
    List<CameraCredentialsDTO> cameras = cameraRepository.getCameraCredentialsOfOrganization(1L);
    CameraCredentialsDTO camera = cameras.getFirst();

    NetSDKLib.LLong loginHandle = DahuaLogin.login(
            camera.getIpAddress(),
            camera.getPort(),
            camera.getUsername(),
            camera.getPassword(),
            sdkInstance
    );

    BufferedImage image = ImageIO.read(new File("SourceFaces/5.jpg"));
    byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

    ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
    buffer.put(pixels);
    buffer.flip();

    Pointer pointer = Native.getDirectBufferPointer(buffer);

    NetSDKLib.NET_IN_OPERATE_FACERECONGNITIONDB netIn = new NetSDKLib.NET_IN_OPERATE_FACERECONGNITIONDB();
    NetSDKLib.NET_OUT_OPERATE_FACERECONGNITIONDB netOut = new NetSDKLib.NET_OUT_OPERATE_FACERECONGNITIONDB();
    netIn.stPersonInfo = new NetSDKLib.FACERECOGNITION_PERSON_INFO();
    netIn.stPersonInfo.szPersonName = "test".getBytes();
    byte[] groupBytes = ("Research" + "\0").getBytes(StandardCharsets.UTF_8);

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(groupBytes.length);
    byteBuffer.put(groupBytes);

    ByteBuffer intBuffer = ByteBuffer.allocateDirect(4);
    intBuffer.putInt(1);

    netIn.stPersonInfo.pszGroupName = Native.getDirectBufferPointer(byteBuffer);
    netIn.stPersonInfo.pszGroupID = Native.getDirectBufferPointer(intBuffer);
    netIn.stPersonInfo.bGroupIdLen = (byte) NET_GROUPID_LENGTH;
    netIn.stPersonInfo.bGroupNameLen = (byte) NET_GROUPNAME_LENGTH;


    netIn.emOperateType = NetSDKLib.EM_OPERATE_FACERECONGNITIONDB_TYPE.NET_FACERECONGNITIONDB_ADD;
    netIn.pBuffer = pointer;
    netIn.nBufferLen = buffer.capacity();
//    netIn.bUsePersonInfoEx = 1;
//    netIn.stPersonInfoEx = new NetSDKLib.FACERECOGNITION_PERSON_INFOEX();
//    netIn.stPersonInfoEx.szPersonName = "test".getBytes();
//    netIn.stPersonInfoEx.szGroupID = "1".getBytes();
//    netIn.stPersonInfoEx.szGroupName = "Research".getBytes();

    NetSDKLib netsdkInstance = NetSDKLib.NETSDK_INSTANCE;
    boolean exists = netsdkInstance.CLIENT_OperateFaceRecognitionDB(loginHandle, netIn, netOut, 1000);
    log.info("{}", netOut.nErrorCodeNum);
    log.info(exists ? "exists" : "doesnt");

  }

  @PostMapping()
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerCamera(@RequestBody RegisterCameraDTO cameraToRegister) throws
          CameraAlreadyExistsInOrganizationException, OrganizationDoesntExistException {
    Organization organization = organizationServices.getOrganization(cameraToRegister.getOrganizationId());
    cameraServices.registerCamera(cameraToRegister, organization);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfOrganization(@PathVariable Long organizationId) throws OrganizationDoesntExistException {
    boolean organizationExists = organizationServices.organizationExists(organizationId);
    if (organizationExists) {
      List<GetCameraDTO> organizationCameras = cameraServices.getCamerasOfOrganization(organizationId);
      return new ResponseEntity<>(organizationCameras, HttpStatus.OK);
    }
    return null;
  }

  @PostMapping("start-face-recognition/organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> startFaceRecognitionOfOrganization(@PathVariable Long organizationId) throws NoInCameraExistsException {
    cameraSubscriptionServices.startFaceRecognitionSubscription(organizationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("stop-face-recognition/organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> stopFaceRecognitionOfOrganization(@PathVariable Long organizationId) {
    cameraSubscriptionServices.stopFaceRecognitionSubscription(organizationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
