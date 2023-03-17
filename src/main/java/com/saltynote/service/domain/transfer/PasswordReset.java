package com.saltynote.service.domain.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordReset {
    @NotBlank
    private String token;
    @NotBlank
    private String password;
}
