package com.fraud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByNameAndPassword(String name, String password);
    User findByName(String name);
}