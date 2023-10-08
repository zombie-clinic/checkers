package com.example.demo.service;

import com.example.demo.domain.UserResponse;

public interface UserService {

    UserResponse findUserById(Long userId);
}
