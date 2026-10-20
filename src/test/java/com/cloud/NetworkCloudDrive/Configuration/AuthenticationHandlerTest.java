package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationHandlerTest {

    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private AuthenticationHandler handler;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        handler = new AuthenticationHandler();
        injectDao(handler, sqLiteDAO);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @SuppressWarnings("unchecked")
    private void injectDao(AuthenticationHandler handler, SQLiteDAO dao) throws Exception {
        java.lang.reflect.Field field = AuthenticationHandler.class.getDeclaredField("sqLiteDAO");
        field.setAccessible(true);
        field.set(handler, dao);
    }

    @Test
    void onAuthenticationSuccess_WritesJsonResponseWithUserData() throws IOException, ServletException {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "pwd");
        UserEntity user = new UserEntity("user", "user@example.com", "pwd", com.cloud.NetworkCloudDrive.Models.Enum.UserRole.NORMAL_USER);
        user.setId(5L);
        when(sqLiteDAO.findUserByMail("user@example.com")).thenReturn(user);
        when(sqLiteDAO.saveUser(user)).thenReturn(user);

        handler.onAuthenticationSuccess(request, response, auth);

        verify(response).setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        verify(response).flushBuffer();
        verify(sqLiteDAO).findUserByMail("user@example.com");
        verify(sqLiteDAO).saveUser(any(UserEntity.class));
        assertFalse(stringWriter.toString().isEmpty());
    }

    @Test
    void onAuthenticationFailure_Returns401WithErrorJson() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        handler.onAuthenticationFailure(request, response, exception);

        verify(response).setStatus(401);
        verify(response).setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        verify(response).flushBuffer();
        String output = stringWriter.toString();
        assertFalse(output.isEmpty());
        assertTrue(output.contains("Login failure"));
    }
}
