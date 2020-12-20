package com.saltynote.service.utils;

import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

public class BaseUtils {
  private static String baseUrl = "https://saltynote.com";

  // This is used for test or dev usage, do not call it in prod.
  public static void setBaseUrl(String _baseUrl) {
    if (StringUtils.startsWithIgnoreCase(_baseUrl, "http")) {
      baseUrl = _baseUrl;
    }
  }

  public static String getConfirmationUrl(@NotNull String secret) {
    return baseUrl + "/email/verification?confirmation=" + secret;
  }
}
