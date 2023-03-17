package com.saltynote.service.utils;

import com.saltynote.service.exception.IllegalInitialException;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

public class BaseUtils {
    private static String baseUrl = "https://saltynote.com";

    private BaseUtils() {
        throw new IllegalInitialException("Do not instantiate me.");
    }

    // This is used for test or dev usage, do not call it in prod.
    public static void setBaseUrl(String baseUrl) {
        if (StringUtils.startsWithIgnoreCase(baseUrl, "http")) {
            BaseUtils.baseUrl = baseUrl;
        }
    }

    public static String getConfirmationUrl(@NotNull String secret) {
        return baseUrl + "/email/verification?confirmation=" + secret;
    }

    public static String getPasswordResetUrl(@NotNull String secret) {
        return baseUrl + "/password/reset?token=" + secret;
    }

    public static boolean containsAllIgnoreCase(String src, Iterable<String> queries) {
        if (StringUtils.isBlank(src)) {
            return false;
        }
        for (String q : queries) {
            if (StringUtils.isNotBlank(q) && !StringUtils.containsIgnoreCase(src, q.trim())) {
                return false;
            }
        }
        return true;
    }
}
