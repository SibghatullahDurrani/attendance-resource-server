package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServicesImplTest {
  @Mock
  private DepartmentRepository departmentRepository;

  @InjectMocks
  private DepartmentServicesImpl departmentServices;

//  @Test
//  public void departmentExist_ThrowsDepartmentDoesntExistException(){
//    depart
//    when(departmentRepository.existsById())
//
//  }
}