package com.barco.auth.service;


import com.barco.model.dto.ResponseDTO;
import com.barco.model.dto.UserDTO;

public interface AppUserService {

    ResponseDTO saveUserRegistration(UserDTO userDTO);

    ResponseDTO emailTokenVerification(String token);

    ResponseDTO forgetPassword(String email);

    ResponseDTO resetPassword(UserDTO userDTO);

}
