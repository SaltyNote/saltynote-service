package com.saltynote.service.domain.transfer;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    @NotBlank
    @javax.validation.constraints.Email
    private String email;
}
