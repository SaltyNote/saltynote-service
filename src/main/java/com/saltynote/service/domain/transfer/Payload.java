package com.saltynote.service.domain.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payload {
    @NotBlank
    @javax.validation.constraints.Email
    private String email;
}
