package com.saltynote.service.repository;

import com.saltynote.service.entity.LoginHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginHistoryRepository extends MongoRepository<LoginHistory, Integer> {

    void deleteByUserId(String userId);

}
