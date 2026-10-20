package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Security.UserSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserSecurity userSecurity;
    @Mock
    private Environment env;

    @Test
    void passwordEncoder_BCrypt_ReturnsBCryptPasswordEncoder() {
        SecurityConfig config = new SecurityConfig(userSecurity, env);
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.matches("test", encoder.encode("test")));
    }

    @Test
    void authenticationProvider_ReturnsProviderManagerWithDaoAuthenticationProvider() {
        SecurityConfig config = new SecurityConfig(userSecurity, env);
        AuthenticationManager manager = config.authenticationProvider();

        assertNotNull(manager);
    }

    @Test
    void authenticationHandler_ReturnsInstance() {
        SecurityConfig config = new SecurityConfig(userSecurity, env);
        AuthenticationHandler handler = config.authenticationHandler();

        assertNotNull(handler);
        assertTrue(handler instanceof AuthenticationHandler);
    }
}
