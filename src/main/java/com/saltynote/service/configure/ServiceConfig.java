package com.saltynote.service.configure;

import com.saltynote.service.generator.IdGenerator;
import com.saltynote.service.generator.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${saltynote.datacenter.id}")
    private int datacenterId;

    @Value("${saltynote.machine.id}")
    private int machineId;

    @Bean
    public IdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(datacenterId, machineId);
    }

}
