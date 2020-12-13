package com.saltynote.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.SiteUser;

public interface UserRepository
    extends JpaRepository<SiteUser, Integer>, JpaSpecificationExecutor<SiteUser> {

  SiteUser findByUsername(String username);
}
