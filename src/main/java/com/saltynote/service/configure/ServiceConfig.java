package com.saltynote.service.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${saltynote.datacenter.id}")
    private int datacenterId;

    @Value("${saltynote.machine.id}")
    private int machineId;

}
