package com.saltynote.service.domain;

import lombok.Getter;

public enum VaultType {
  PASSWORD("password"),
  NEW_ACCOUNT("new_account"),
  REFRESH_TOKEN("refresh_token");

  @Getter private final String value;

  VaultType(String value) {
    this.value = value;
  }
}
