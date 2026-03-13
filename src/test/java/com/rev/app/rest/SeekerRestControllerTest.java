package com.rev.app.rest;

import com.rev.app.dto.JobSeekerDto;

import com.rev.app.security.CustomUserDetails;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtTokenProvider;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IJobSeekerService;
import com.rev.app.service.IResumeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SeekerRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SeekerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IJobSeekerService jobSeekerService;
    @MockBean
    private IResumeService resumeService;
    @MockBean
    private IApplicationService applicationService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private CustomUserDetails seekerUser;

    @BeforeEach
    void setUp() {
        seekerUser = new CustomUserDetails(1L, "seeker@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SEEKER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                seekerUser, null, seekerUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfile_Success() throws Exception {
        JobSeekerDto dto = new JobSeekerDto();
        dto.setId(10L);
        dto.setName("John Doe");

        when(jobSeekerService.getProfileByUserId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/seeker/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.id").value(10));
    }
}
