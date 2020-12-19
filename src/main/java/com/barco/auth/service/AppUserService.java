package com.barco.auth.service;

import com.barco.model.dto.PagingDto;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.SearchTextDto;
import com.barco.model.dto.UserDTO;

/**
 * @author Nabeel Ahmed
 */
public interface AppUserService {

    ResponseDTO saveUserRegistration(UserDTO userDTO) throws Exception;

    ResponseDTO saveUserRegistrationByAdmin(UserDTO userDTO) throws Exception;

    ResponseDTO emailTokenVerification(String token);

    ResponseDTO forgetPassword(String email);

    ResponseDTO resetPassword(UserDTO userDTO);

    ResponseDTO fetchSuperAdminUserList(Long superAdminId);

    ResponseDTO findAllAdminUsersInPagination(PagingDto paging, Long adminId, SearchTextDto searchTextDto, String startDate, String endDate);

}
