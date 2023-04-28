package com.saltynote.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@Accessors(chain = true)
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 40)
    @NotNull
    @Column(name = "user_id", nullable = false, length = 40)
    private String userId;

    @Size(max = 128)
    @Column(name = "remote_ip", length = 128)
    private String remoteIp;

    @Size(max = 256)
    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @NotNull
    @Column(name = "login_time", nullable = false)
    private Timestamp loginTime;

    @Column(name = "user_idx")
    private Long userIdx;

    @PrePersist
    private void beforeSave() {
        this.loginTime = new Timestamp(System.currentTimeMillis());
    }

}