package com.cufcuf.backend.service;

import java.util.Optional;

import com.cufcuf.backend.model.User;

public interface UserService {
    User registerUser(String fullName, String email, String password);
    Optional<User> loginUser(String email, String password);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}