package com.saltynote.service.domain.transfer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserNewRequest extends UserCredential {
    @NotBlank
    private String token;
}
