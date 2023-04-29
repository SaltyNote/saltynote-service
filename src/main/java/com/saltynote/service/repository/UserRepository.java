package com.saltynote.service.repository;

import com.saltynote.service.entity.SiteUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<SiteUser, String> {

    SiteUser findByUsername(String username);

    Optional<SiteUser> findByEmail(String email);

}
