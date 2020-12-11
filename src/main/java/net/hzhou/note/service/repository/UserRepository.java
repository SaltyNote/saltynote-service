package net.hzhou.note.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.hzhou.note.service.entity.SiteUser;

public interface UserRepository
    extends JpaRepository<SiteUser, String>, JpaSpecificationExecutor<SiteUser> {

  SiteUser findByUsername(String username);
}
