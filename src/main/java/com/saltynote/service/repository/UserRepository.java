package com.saltynote.service.repository;

import com.saltynote.service.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, String>, JpaSpecificationExecutor<SiteUser> {

    SiteUser findByUsername(String username);

    Optional<SiteUser> findByEmail(String email);

}
