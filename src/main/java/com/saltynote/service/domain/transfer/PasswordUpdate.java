package com.saltynote.service.domain.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PasswordUpdate {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String password;

}
