package com.investrocket.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.exception.UserNotFoundException;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User requireUser(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(UserNotFoundException::new);
    }
}
