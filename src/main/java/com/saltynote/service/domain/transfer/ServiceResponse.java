package com.saltynote.service.domain.transfer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {

    private HttpStatus status;

    private String message;

    @JsonIgnore
    public static ServiceResponse ok(String message) {
        return new ServiceResponse(HttpStatus.OK, message);
    }

}
