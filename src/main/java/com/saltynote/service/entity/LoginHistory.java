package com.saltynote.service.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@Accessors(chain = true)
public class LoginHistory {

    @Id
    private String id;

    private String userId;

    private String remoteIp;

    private String userAgent;

    private Long loginTime = System.currentTimeMillis();

}