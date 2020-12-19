package com.saltynote.service.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.saltynote.service.domain.VaultEntity;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
class VaultServiceTest {

  @Autowired private VaultService vaultService;

  @Test
  void encodeAndDecodeTest() throws IOException {
    VaultEntity ve = new VaultEntity().setSecret("secret").setUserId("8888");
    String encoded = vaultService.encode(ve);
    VaultEntity decoded = vaultService.decode(encoded);
    assertEquals(ve, decoded);
  }
}
