package com.saltynote.service.domain.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordReset {
    @NotBlank
    private String token;
    @NotBlank
    private String password;
}
