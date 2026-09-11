package com.evidlock.persistence.dao;

import com.evidlock.persistence.model.User;

import java.util.List;

public interface UserDao {
    User findByEmail(String email);
    User findByToken(String token);
    User findById(Long id);
    List<User> findByAgency(String agencyCode);
    List<User> getAll();
    boolean insert(User user);
    boolean updatePassword(User user);
    boolean updateEmail(User user);
}
