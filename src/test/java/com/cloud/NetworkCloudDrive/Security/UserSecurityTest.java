package com.cloud.NetworkCloudDrive.Security;

import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSecurityTest {

    @Mock
    private SQLiteDAO sqLiteDAO;

    private UserSecurity userSecurity;

    @BeforeEach
    void setUp() {
        userSecurity = new UserSecurity(sqLiteDAO);
    }

    @Test
    void loadUserByUsername_NormalUser_ReturnsUserDetailsWithRoleUser() {
        UserEntity user = new UserEntity("alice", "alice@example.com", "encoded-pwd", UserRole.NORMAL_USER);
        user.setId(1L);
        when(sqLiteDAO.findUserByMail("alice@example.com")).thenReturn(user);

        UserDetails result = userSecurity.loadUserByUsername("alice@example.com");

        assertEquals("alice@example.com", result.getUsername());
        assertEquals("encoded-pwd", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(sqLiteDAO).findUserByMail("alice@example.com");
    }

    @Test
    void loadUserByUsername_UsernameConvertedToLowerCase() {
        UserEntity user = new UserEntity("bob", "bob@example.com", "pwd", UserRole.NORMAL_USER);
        when(sqLiteDAO.findUserByMail("bob@example.com")).thenReturn(user);

        userSecurity.loadUserByUsername("BOB@EXAMPLE.COM");

        verify(sqLiteDAO).findUserByMail("bob@example.com");
    }

    @Test
    void loadUserByUsername_AdminUser_ReturnsRoleAdmin() {
        UserEntity user = new UserEntity("admin", "admin@example.com", "admin-pwd", UserRole.ADMIN);
        when(sqLiteDAO.findUserByMail("admin@example.com")).thenReturn(user);

        UserDetails result = userSecurity.loadUserByUsername("admin@example.com");

        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_GuestUser_ReturnsRoleGuest() {
        UserEntity user = new UserEntity("guest", "guest@example.com", "guest-pwd", UserRole.GUEST);
        when(sqLiteDAO.findUserByMail("guest@example.com")).thenReturn(user);

        UserDetails result = userSecurity.loadUserByUsername("guest@example.com");

        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST")));
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(sqLiteDAO.findUserByMail("missing@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class,
                () -> userSecurity.loadUserByUsername("missing@example.com"));
    }
}
