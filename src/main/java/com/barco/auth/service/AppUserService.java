package com.barco.auth.service;

import com.barco.model.dto.PagingDto;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.SearchTextDto;
import com.barco.model.dto.UserDTO;
import org.springframework.data.domain.Pageable;

/**
 * @author Nabeel Ahmed
 */
public interface AppUserService {

    ResponseDTO saveUserRegistration(UserDTO userDTO) throws Exception;

    ResponseDTO saveUserRegistrationByAdmin(UserDTO userDTO) throws Exception;

    ResponseDTO emailTokenVerification(String token) throws Exception;

    ResponseDTO forgetPassword(String email) throws Exception;

    ResponseDTO resetPassword(UserDTO userDTO) throws Exception;

    ResponseDTO fetchSuperAdminUserList(Long superAdminId) throws Exception;

    ResponseDTO findAllAdminUsersInPagination(Pageable paging, Long adminId, SearchTextDto searchTextDto,
          String startDate, String endDate, String order, String columnName) throws Exception;

}
