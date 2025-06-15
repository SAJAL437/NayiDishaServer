package com.impact.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.impact.entity.User;
import com.impact.exception.UserException;
import com.impact.request.SignupRequest;

public interface UserServices {
    User findUserProfileByJwt(String jwt) throws UserException;

    public void registerUser(SignupRequest signupRequest) throws UserException;

    User save(User user);

    public User update(User user, Long id) throws UserException;

    public void delete(Long id) throws UserException;

    User getUserbyid(Long id) throws UserException;

    public User getUserByEmail(String email) throws UserException;

    public void verifyUser(String token) throws UserException;

    Page<User> getAllUsersWithUserRole(Pageable pageable) throws UserException;

    // pagination

    List<User> getAllUsers() throws UserException;

}
