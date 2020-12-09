package net.hzhou.demo.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.hzhou.demo.jwt.entity.User;

public interface UserRepository
    extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

  User findByUsername(String username);
}
