package com.hourai.prts.service;

import com.hourai.prts.entity.User;
import org.junit.jupiter.api.*;
import java.util.List;

public class UserServiceTest {
    private static UserService userService;

    @BeforeAll
    public static void setup() {
        userService = new UserService();
    }

    @Test
    public void testRegisterAndQuery() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        user.setNickname("测试用户");
        user.setAdmin(false);
        user.setStatus(true);
        int result = userService.register(user);
        Assertions.assertTrue(result > 0);

        List<User> users = userService.getAllUsers();
        Assertions.assertTrue(users.stream().anyMatch(u -> "testuser".equals(u.getUsername())));
    }

    @Test
    public void testUpdateAndDelete() throws Exception {
        List<User> users = userService.getAllUsers();
        User user = users.stream().filter(u -> "testuser".equals(u.getUsername())).findFirst().orElse(null);
        Assertions.assertNotNull(user);
        user.setNickname("新昵称");
        int updateResult = userService.updateUser(user);
        Assertions.assertTrue(updateResult > 0);

        int deleteResult = userService.deleteUser(user.getId());
        Assertions.assertTrue(deleteResult > 0);
    }
}
