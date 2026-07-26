package com.hourai.prts.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hourai.prts.entity.Announcement;
import com.hourai.prts.entity.NotificationState;
import com.hourai.prts.entity.Question;
import com.hourai.prts.entity.User;
import com.hourai.prts.repository.AnnouncementRepository;
import com.hourai.prts.repository.ExamRecordRepository;
import com.hourai.prts.repository.NotificationStateRepository;
import com.hourai.prts.repository.QuestionRepository;
import com.hourai.prts.repository.UserRepository;
import com.hourai.prts.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 核心业务集成测试。
 *
 * <p>使用 H2 的 MySQL 兼容模式启动完整 Spring 上下文，覆盖安全过滤器、
 * Controller、Service、JPA Repository 和事务，不依赖开发机上的 MySQL 数据。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CoreWorkflowIntegrationTest {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamRecordRepository examRecordRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;
    @Autowired
    private NotificationStateRepository notificationStateRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void registerLoginAndProfileFormACompleteAuthenticationCycle() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new_doctor",
                                  "password": "secure123",
                                  "email": "doctor@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("new_doctor"));

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new_doctor","password":"secure123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginBody).path("data").path("token").asText();

        mockMvc.perform(get("/api/v1/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("new_doctor"))
                .andExpect(jsonPath("$.data.isAdmin").value(false));
    }

    @Test
    void securityMatrixAndExamSubmissionUseAuthenticatedUser() throws Exception {
        User ordinary = saveUser("ordinary", false);
        User admin = saveUser("admin_test", true);
        String ordinaryToken = tokenFor(ordinary);
        String adminToken = tokenFor(admin);
        String questionJson = """
                {"type":1,"difficulty":1,"question":"2+2=?","options":["3","4"],"answer":"2"}
                """;

        mockMvc.perform(post("/api/v1/questions")
                        .header("Authorization", "Bearer " + ordinaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isOk());

        Question question = questionRepository.findAll().get(0);
        String paperResponse = mockMvc.perform(get("/api/v1/exam/paper")
                        .header("Authorization", "Bearer " + ordinaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].answer").doesNotExist())
                .andExpect(jsonPath("$.data.questions[0].analysis").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long paperId = objectMapper.readTree(paperResponse)
                .path("data").path("paperId").asLong();

        mockMvc.perform(post("/api/v1/exam/submit")
                        .header("Authorization", "Bearer " + ordinaryToken)
                        // 即使客户端夹带其他 userId，后端也只使用 JWT principal。
                        .param("userId", String.valueOf(admin.getId()))
                        .param("paperId", String.valueOf(paperId))
                        .param("answers", question.getId() + ":2")
                        .param("duration", "20")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(100.0))
                .andExpect(jsonPath("$.data.questions[0].answer").value(2));

        assertEquals(1, examRecordRepository.count());
        assertEquals(ordinary.getId(), examRecordRepository.findAll().get(0).getUserId());
    }

    @Test
    void notificationListExcludesExpiredAndUserHiddenAnnouncements() throws Exception {
        User user = saveUser("notification_user", false);
        Announcement visible = saveAnnouncement(
                "可见公告", LocalDateTime.now().plusDays(1));
        Announcement hidden = saveAnnouncement(
                "已隐藏公告", LocalDateTime.now().plusDays(1));
        saveAnnouncement("已过期公告", LocalDateTime.now().minusDays(1));

        NotificationState state = new NotificationState();
        state.setUserId(user.getId());
        state.setNotificationId(hidden.getId());
        state.setIsHidden(true);
        notificationStateRepository.save(state);

        String response = mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + tokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.length()").value(1))
                .andExpect(jsonPath("$.data.notifications[0].title").value("可见公告"))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andReturn().getResponse().getContentAsString();

        JsonNode firstNotification = objectMapper.readTree(response)
                .path("data").path("notifications").get(0);
        assertEquals(visible.getId(), firstNotification.path("id").asLong());
    }

    private User saveUser(String username, boolean admin) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsAdmin(admin);
        user.setStatus(true);
        user.setRegisterTime(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return tokenProvider.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
    }

    private Announcement saveAnnouncement(String title, LocalDateTime expiresAt) {
        Announcement announcement = new Announcement();
        announcement.setType("system");
        announcement.setTitle(title);
        announcement.setContent(title + "内容");
        announcement.setCreatedAt(LocalDateTime.now().format(TIME_FORMATTER));
        announcement.setExpiresAt(expiresAt.format(TIME_FORMATTER));
        return announcementRepository.save(announcement);
    }
}
