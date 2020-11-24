package com.barco.auth.service;

import com.barco.model.dto.PaggingDto;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.SearchTextDto;
import com.barco.model.dto.UserDTO;
import com.barco.model.util.PaggingUtil;

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

    ResponseDTO getAllUsers(PaggingDto pagging, Long loggedInUserId, SearchTextDto searchTextDto, Long userType, String startDate, String endDate);

}
