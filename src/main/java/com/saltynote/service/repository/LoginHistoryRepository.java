package com.saltynote.service.repository;

import com.saltynote.service.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Integer>, JpaSpecificationExecutor<LoginHistory> {

    void deleteByUserId(String userId);

}
