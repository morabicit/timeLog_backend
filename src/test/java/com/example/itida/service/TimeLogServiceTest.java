package com.example.itida.service;

import com.example.itida.dto.TimeLogResponseDto;
import com.example.itida.entity.TimeLog;
import com.example.itida.entity.User;
import com.example.itida.repository.TimeLogRepository;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeLogServiceTest {
    @Mock
    private TimeLogRepository timeLogRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private TimeLogService timeLogService;
    @Captor
    private ArgumentCaptor<TimeLog> timeLogCaptor;

    private User testUser;
    private LocalDate today;
    private LocalTime loginTime;
    private LocalTime logoutTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        today = LocalDate.now();
        loginTime = LocalTime.of(9, 0);
        logoutTime = LocalTime.of(17, 0);
    }

    @Test
    void getTodayTimeLog_ExistingLog() {
        TimeLog mockLog = new TimeLog();
        mockLog.setDate(today);
        mockLog.setLoginTime(loginTime);
        mockLog.setLogoutTime(logoutTime);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDate(testUser, today))
                .thenReturn(Optional.of(mockLog));
        TimeLogResponseDto result = timeLogService.getTodayTimeLog();
        assertNotNull(result);
        assertEquals(today, result.getDate());
        assertEquals(loginTime, result.getLoginTime());
        assertEquals(logoutTime, result.getLogoutTime());
    }

    @Test
    void getTodayTimeLog_NoExistingLog() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDate(testUser, today))
                .thenReturn(Optional.empty());

        TimeLogResponseDto result = timeLogService.getTodayTimeLog();
        assertNull(result);
    }

    @Test
    void submitTimeLog_NewTimeLog() throws ValidationException {
        TimeLog newLog = new TimeLog();
        newLog.setDate(today);
        newLog.setLoginTime(loginTime);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDate(testUser, today))
                .thenReturn(Optional.empty());
        when(timeLogRepository.save(any(TimeLog.class))).thenAnswer(inv -> inv.getArgument(0));
        TimeLogResponseDto result = timeLogService.submitTimeLog(newLog);

        verify(timeLogRepository).save(timeLogCaptor.capture());
        TimeLog savedLog = timeLogCaptor.getValue();

        assertEquals(testUser, savedLog.getUser());
        assertEquals(today, savedLog.getDate());
        assertEquals(loginTime, savedLog.getLoginTime());
        assertNull(savedLog.getLogoutTime());
        assertEquals(today, result.getDate());
        assertEquals(loginTime, result.getLoginTime());
    }

    @Test
    void submitTimeLog_UpdateExistingLog() throws ValidationException {
        TimeLog existingLog = new TimeLog();
        existingLog.setUser(testUser);
        existingLog.setDate(today);
        existingLog.setLoginTime(loginTime.minusHours(1));
        existingLog.setLogoutTime(logoutTime);

        TimeLog updateLog = new TimeLog();
        updateLog.setDate(today);
        updateLog.setLoginTime(loginTime);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDate(testUser, today))
                .thenReturn(Optional.of(existingLog));
        when(timeLogRepository.save(any(TimeLog.class))).thenAnswer(inv -> inv.getArgument(0));
        TimeLogResponseDto result = timeLogService.submitTimeLog(updateLog);
        verify(timeLogRepository).save(timeLogCaptor.capture());
        TimeLog updatedLog = timeLogCaptor.getValue();

        assertEquals(loginTime, updatedLog.getLoginTime());
        assertEquals(result.getLoginTime(), loginTime);
    }

    @Test
    void submitTimeLog_InsertLogoutTime() throws ValidationException {
        TimeLog existingLog = new TimeLog();
        existingLog.setUser(testUser);
        existingLog.setDate(today);
        existingLog.setLoginTime(loginTime);

        TimeLog updateLog = new TimeLog();
        updateLog.setDate(today);
        updateLog.setLogoutTime(logoutTime);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDate(testUser, today))
                .thenReturn(Optional.of(existingLog));
        when(timeLogRepository.save(any(TimeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeLogResponseDto result = timeLogService.submitTimeLog(updateLog);
        verify(timeLogRepository).save(timeLogCaptor.capture());
        TimeLog updatedLog = timeLogCaptor.getValue();
        assertEquals(loginTime, updatedLog.getLoginTime());
        assertEquals(logoutTime, updatedLog.getLogoutTime());
        assertEquals(result.getLogoutTime(), logoutTime);
    }

    @Test
    void submitTimeLog_BothTimesNull_ThrowsValidationException() {
        TimeLog invalidLog = new TimeLog();
        invalidLog.setDate(today);
        when(userService.getCurrentUser()).thenReturn(testUser);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> timeLogService.submitTimeLog(invalidLog));

        assertEquals("You have to enter either login or logout time.", exception.getMessage());
        verify(timeLogRepository, never()).save(any());
    }

    @Test
    void getTimeLogsByUserAndDateRange() {
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;

        TimeLog log1 = new TimeLog();
        log1.setDate(startDate);
        log1.setLoginTime(loginTime);

        TimeLog log2 = new TimeLog();
        log2.setDate(endDate);
        log2.setLoginTime(loginTime);
        log2.setLogoutTime(logoutTime);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDateBetween(testUser, startDate, endDate))
                .thenReturn(List.of(log1, log2));
        List<TimeLogResponseDto> result = timeLogService.getTimeLogsByUserAndDateRange(startDate, endDate);
        assertEquals(2, result.size());
        assertEquals(startDate, result.get(0).getDate());
        assertEquals(endDate, result.get(1).getDate());
        verify(timeLogRepository).findByUserAndDateBetween(testUser, startDate, endDate);
    }

    @Test
    void getTimeLogsByUserAndDateRange_NoLogs() {
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDateBetween(testUser, startDate, endDate))
                .thenReturn(Collections.emptyList());
        List<TimeLogResponseDto> result = timeLogService.getTimeLogsByUserAndDateRange(startDate, endDate);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTimeLogsByUserAndDateRange_InvalidDateRange() {
        LocalDate startDate = today;
        LocalDate endDate = today.minusDays(10); // Invalid range

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(timeLogRepository.findByUserAndDateBetween(testUser, startDate, endDate))
                .thenReturn(Collections.emptyList());
        List<TimeLogResponseDto> result = timeLogService.getTimeLogsByUserAndDateRange(startDate, endDate);
        assertTrue(result.isEmpty());
        verify(timeLogRepository).findByUserAndDateBetween(testUser, startDate, endDate);
    }
}
