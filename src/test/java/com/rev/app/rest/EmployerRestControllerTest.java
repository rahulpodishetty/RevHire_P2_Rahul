package com.rev.app.rest;

import com.rev.app.dto.EmployerDto;
import com.rev.app.dto.JobDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtTokenProvider;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IEmployerService;
import com.rev.app.service.IJobService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployerRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class EmployerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEmployerService employerService;
    @MockBean
    private IJobService jobService;
    @MockBean
    private IApplicationService applicationService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private CustomUserDetails employerUser;

    @BeforeEach
    void setUp() {
        employerUser = new CustomUserDetails(1L, "employer@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                employerUser, null, employerUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfile_Success() throws Exception {
        EmployerDto dto = new EmployerDto();
        dto.setId(10L);
        dto.setDesignation("HR Manager");

        when(employerService.getProfileByUserId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/employer/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("HR Manager"));
    }

    @Test
    void getMyJobs_Success() throws Exception {
        EmployerDto profile = new EmployerDto();
        profile.setId(10L);

        JobDto jobDto = new JobDto();
        jobDto.setId(1L);
        jobDto.setTitle("Developer");

        Page<JobDto> page = new PageImpl<>(List.of(jobDto), PageRequest.of(0, 10), 1);

        when(employerService.getProfileByUserId(1L)).thenReturn(profile);
        when(jobService.getJobsByEmployer(eq(10L), isNull(), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/employer/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Developer"));
    }
}
