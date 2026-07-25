package com.hourai.prts.service;

import com.hourai.prts.entity.User;
import com.hourai.prts.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void registerHashesPasswordAndCreatesEnabledOrdinaryUser() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(repository, encoder);
        when(repository.existsByUsername("doctor")).thenReturn(false);
        when(encoder.encode("plain-password")).thenReturn("bcrypt-hash");
        when(repository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.register("doctor", "plain-password", "doctor@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("bcrypt-hash", saved.getPassword());
        assertFalse(saved.getIsAdmin());
        assertEquals(true, saved.getStatus());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository, mock(PasswordEncoder.class));
        when(repository.existsByUsername("doctor")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service.register("doctor", "password", null));
    }

    @Test
    void onlySuperAdminCanDemoteAnAdministrator() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository, mock(PasswordEncoder.class));
        User ordinaryAdmin = user(2L, true);
        User target = user(3L, true);
        when(repository.findById(2L)).thenReturn(Optional.of(ordinaryAdmin));
        when(repository.findById(3L)).thenReturn(Optional.of(target));

        assertThrows(RuntimeException.class,
                () -> service.setAdminStatus(2L, 3L, false));
    }

    private User user(Long id, boolean admin) {
        User user = new User();
        user.setId(id);
        user.setIsAdmin(admin);
        return user;
    }
}
