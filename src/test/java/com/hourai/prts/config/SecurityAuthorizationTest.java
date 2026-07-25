package com.hourai.prts.config;

import com.hourai.prts.controller.QuestionController;
import com.hourai.prts.entity.Question;
import com.hourai.prts.security.JwtAuthenticationFilter;
import com.hourai.prts.security.JwtTokenProvider;
import com.hourai.prts.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        when(questionService.getFilteredQuestions(any(), any(), any())).thenReturn(List.of());
        when(questionService.addQuestion(any())).thenAnswer(invocation -> {
            Question question = invocation.getArgument(0);
            question.setId(1L);
            return question;
        });
    }

    @Test
    void questionReadIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/questions"))
                .andExpect(status().isOk());
    }

    @Test
    void ordinaryUserCannotCreateQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/questions")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":1,"difficulty":1,"question":"q","options":["A","B"],"answer":"1"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/questions")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":1,"difficulty":1,"question":"q","options":["A","B"],"answer":"1"}
                                """))
                .andExpect(status().isOk());
    }
}
