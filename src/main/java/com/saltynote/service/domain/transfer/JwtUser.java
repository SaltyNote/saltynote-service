package com.saltynote.service.domain.transfer;

import com.saltynote.service.domain.IdentifiableUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtUser implements IdentifiableUser {
    private String id;
    private String username;
}
