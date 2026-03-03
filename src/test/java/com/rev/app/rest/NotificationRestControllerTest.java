package com.rev.app.rest;

import com.rev.app.dto.NotificationDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtTokenProvider;
import com.rev.app.service.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class NotificationRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private INotificationService notificationService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private CustomUserDetails buildSeekerUser() {
                return new CustomUserDetails(1L, "seeker@test.com", "password",
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SEEKER")));
        }

        private void setAuth(CustomUserDetails userDetails) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        void getUnreadCount_Success() throws Exception {
                CustomUserDetails userDetails = buildSeekerUser();
                setAuth(userDetails);

                when(notificationService.getUnreadCount(1L)).thenReturn(5L);

                mockMvc.perform(get("/api/notifications/unread-count")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(5));

                SecurityContextHolder.clearContext();
        }

        @Test
        void getNotifications_Success() throws Exception {
                CustomUserDetails userDetails = buildSeekerUser();
                setAuth(userDetails);

                NotificationDto dto = new NotificationDto();
                dto.setId(10L);
                dto.setMessage("Test Notification");

                when(notificationService.getUserNotifications(1L)).thenReturn(List.of(dto));

                mockMvc.perform(get("/api/notifications")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(10))
                                .andExpect(jsonPath("$[0].message").value("Test Notification"));

                SecurityContextHolder.clearContext();
        }

        @Test
        void getUnreadNotifications_Success() throws Exception {
                CustomUserDetails userDetails = buildSeekerUser();
                setAuth(userDetails);

                NotificationDto dto = new NotificationDto();
                dto.setId(20L);
                dto.setMessage("Unread Notification");

                when(notificationService.getUnreadNotifications(1L)).thenReturn(List.of(dto));

                mockMvc.perform(get("/api/notifications/unread")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(20));

                SecurityContextHolder.clearContext();
        }

        @Test
        void markAsRead_Success() throws Exception {
                CustomUserDetails userDetails = buildSeekerUser();
                setAuth(userDetails);

                mockMvc.perform(post("/api/notifications/10/read")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                SecurityContextHolder.clearContext();
        }
}
