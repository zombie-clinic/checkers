package com.example.demo.service;

import com.example.demo.persistence.UserRepository;
import com.example.demo.domain.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(u -> new UserResponse(u.getId(), u.getUsername()))
                .orElse(null);
    }
}
