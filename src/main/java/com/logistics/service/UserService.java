package com.logistics.service;

import com.logistics.entity.User;
import com.logistics.payload.SignupRequest;

public interface UserService {

    User registerUser(SignupRequest request);
}
