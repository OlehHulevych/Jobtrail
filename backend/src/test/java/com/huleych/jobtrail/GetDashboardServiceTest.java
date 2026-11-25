package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.GetDashboard;
import com.huleych.jobtrail.tracking.application.services.GetDashboardService;
import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import com.huleych.jobtrail.tracking.web.DashboardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetDashboardServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    JobApplicationRepository repository;
    GetDashboardService service;

    @BeforeEach
    public void init() {
        service = new GetDashboardService(new GetDashboard(repository));
    }

    @Test
    public void shouldReturnCountsAndTotal() {
        when(repository.countByStatus(OWNER_ID)).thenReturn(Map.of(
                ApplicationStatus.WISHLIST, 2L,
                ApplicationStatus.APPLIED, 3L,
                ApplicationStatus.OFFER, 1L));

        DashboardResponse result = service.execute(OWNER_ID);

        assertEquals(6, result.total());
        assertEquals(2L, result.byStatus().get(ApplicationStatus.WISHLIST));
        assertEquals(3L, result.byStatus().get(ApplicationStatus.APPLIED));
        assertEquals(1L, result.byStatus().get(ApplicationStatus.OFFER));
    }

    @Test
    public void shouldIncludeAllStatusesWithZeroForMissingOnes() {
        when(repository.countByStatus(OWNER_ID)).thenReturn(Map.of(ApplicationStatus.APPLIED, 4L));

        DashboardResponse result = service.execute(OWNER_ID);

        assertEquals(ApplicationStatus.values().length, result.byStatus().size());
        assertEquals(4L, result.byStatus().get(ApplicationStatus.APPLIED));
        assertEquals(0L, result.byStatus().get(ApplicationStatus.WISHLIST));
        assertEquals(0L, result.byStatus().get(ApplicationStatus.INTERVIEW));
        assertEquals(0L, result.byStatus().get(ApplicationStatus.OFFER));
        assertEquals(0L, result.byStatus().get(ApplicationStatus.REJECTED));
        assertEquals(0L, result.byStatus().get(ApplicationStatus.WITHDRAWN));
    }

    @Test
    public void shouldReturnZeroTotalWhenOwnerHasNoApplications() {
        when(repository.countByStatus(OWNER_ID)).thenReturn(Map.of());

        DashboardResponse result = service.execute(OWNER_ID);

        assertEquals(0, result.total());
        assertEquals(ApplicationStatus.values().length, result.byStatus().size());
        assertTrue(result.byStatus().values().stream().allMatch(count -> count == 0L));
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));
        verifyNoInteractions(repository);
    }
}
