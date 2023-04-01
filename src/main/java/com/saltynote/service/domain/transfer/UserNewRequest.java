package com.saltynote.service.domain.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserNewRequest extends UserCredential {

    @NotBlank
    private String token;

}
