package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.shared.web.GlobalExceptionHandler;
import com.huleych.jobtrail.tracking.application.services.GetDashboardService;
import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.web.DashboardResponse;
import com.huleych.jobtrail.tracking.web.JobApplicationController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class JobApplicationControllerDashboardTest {
    private static final String URL = "/api/applications/dashboard";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    GetDashboardService getDashboardService;
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        User user = new User("Test User", "user@example.com", "hash", Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        JobApplicationController controller = new JobApplicationController(
                null, null, null, null, null, null, null, null, getDashboardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReturnOkWithTotalAndCountsForCurrentUser() throws Exception {
        Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status, 0L);
        }
        byStatus.put(ApplicationStatus.WISHLIST, 2L);
        byStatus.put(ApplicationStatus.APPLIED, 3L);
        when(getDashboardService.execute(USER_ID)).thenReturn(new DashboardResponse(5, byStatus));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.byStatus.WISHLIST").value(2))
                .andExpect(jsonPath("$.byStatus.APPLIED").value(3))
                .andExpect(jsonPath("$.byStatus.INTERVIEW").value(0))
                .andExpect(jsonPath("$.byStatus.OFFER").value(0))
                .andExpect(jsonPath("$.byStatus.REJECTED").value(0))
                .andExpect(jsonPath("$.byStatus.WITHDRAWN").value(0));
    }

    @Test
    public void shouldReturnBadRequestWhenServiceRejectsUserId() throws Exception {
        when(getDashboardService.execute(USER_ID)).thenThrow(new IllegalArgumentException("User id is required"));

        mockMvc.perform(get(URL)).andExpect(status().isBadRequest());
    }
}
