
package com.saltynote.service.utils;

import com.saltynote.service.exception.IllegalInitialException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseUtilsTest {

    @Test
    void exceptionWhenInit() {
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            Constructor<BaseUtils> pcc = BaseUtils.class.getDeclaredConstructor();
            pcc.setAccessible(true);
            BaseUtils baseUtils = pcc.newInstance();
        });

        assertTrue(exception.getCause() instanceof IllegalInitialException);
        assertThat(exception.getCause().getMessage()).isEqualTo("Do not instantiate me.");
    }

    @Test
    void testSetBaseUrl() {
        BaseUtils.setBaseUrl("http://baseUrl");
        assertTrue(BaseUtils.getPasswordResetUrl("secret").startsWith("http://baseUrl"));
    }

    @Test
    void testGetPasswordResetUrl() {
        BaseUtils.setBaseUrl("http://baseUrl");
        assertThat(BaseUtils.getPasswordResetUrl("secret")).isEqualTo("http://baseUrl/password/reset?token=secret");
    }

    @Test
    void testContainsAllIgnoreCase() {
        assertThat(BaseUtils.containsAllIgnoreCase("", List.of("value"))).isFalse();
        assertThat(BaseUtils.containsAllIgnoreCase("src", List.of("value"))).isFalse();
        assertThat(BaseUtils.containsAllIgnoreCase("src is true", List.of("src", "is"))).isTrue();
    }

}
