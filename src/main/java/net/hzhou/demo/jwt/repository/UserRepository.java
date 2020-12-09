package net.hzhou.demo.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.hzhou.demo.jwt.entity.SiteUser;

public interface UserRepository
    extends JpaRepository<SiteUser, String>, JpaSpecificationExecutor<SiteUser> {

  SiteUser findByUsername(String username);
}
