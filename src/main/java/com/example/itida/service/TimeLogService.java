package com.example.itida.service;

import com.example.itida.dto.TimeLogResponseDto;
import com.example.itida.entity.TimeLog;
import com.example.itida.entity.User;
import com.example.itida.repository.TimeLogRepository;
import jakarta.xml.bind.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeLogService {

    private final Logger logger = LoggerFactory.getLogger(TimeLogService.class);
    private final TimeLogRepository timeLogRepository;
    private final UserService userService;

    @Autowired
    public TimeLogService(TimeLogRepository timeLogRepository, UserService userService) {
        this.timeLogRepository = timeLogRepository;
        this.userService = userService;
    }

    public TimeLogResponseDto getTodayTimeLog() {
        logger.info("getTodayTimeLog() called for current user");
        try {
            User user = userService.getCurrentUser();
            logger.debug("Fetching today's time log for user: {}", user.getEmail());
            LocalDate today = LocalDate.now();
            return timeLogRepository.findByUserAndDate(user, today)
                    .map(timeLog -> {
                        return new TimeLogResponseDto(timeLog.getDate(), timeLog.getLoginTime(), timeLog.getLogoutTime());
                    })
                    .orElseGet(() -> {
                        logger.warn("No time log found for user: {} on {}", user.getEmail(), today);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error retrieving time log for user", e);
            return null;
        }
    }

    public TimeLogResponseDto submitTimeLog(TimeLog timeLog) throws ValidationException {
        logger.info("submitTimeLog() called for current user");
        try {
            User user = userService.getCurrentUser();
            logger.debug("Processing time log for user: {}", user.getEmail());
            if (timeLog.getLoginTime() == null && timeLog.getLogoutTime() == null) {
                throw new ValidationException("You have to enter either login or logout time.");
            }
            Optional<TimeLog> existingLog = timeLogRepository.findByUserAndDate(user, timeLog.getDate());
            TimeLog savedTimeLog = existingLog.map(existingTimeLog -> {
                if(timeLog.getLoginTime() != null)
                    existingTimeLog.setLoginTime(timeLog.getLoginTime());
                existingTimeLog.setLogoutTime(timeLog.getLogoutTime());
                logger.debug("Updating existing time log for user: {} on date: {}", user.getEmail(), timeLog.getDate());
                return timeLogRepository.save(existingTimeLog);
            }).orElseGet(() -> {
                timeLog.setUser(user);
                return timeLogRepository.save(timeLog);
            });
            return new TimeLogResponseDto(savedTimeLog.getDate(), savedTimeLog.getLoginTime(), savedTimeLog.getLogoutTime());
        } catch (ValidationException e) {
            logger.error("ValidationException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error processing time log for user: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while submitting the time log", e);
        }
    }

    public List<TimeLogResponseDto> getTimeLogsByUserAndDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("getTimeLogsByUserAndDateRange() called");
        User user = userService.getCurrentUser();
        logger.info("Fetching time logs for user: {} with parameters: startDate={}, endDate={}", user.getEmail(),startDate, endDate);
        List<TimeLog> logs = timeLogRepository.findByUserAndDateBetween(user, startDate, endDate);
        if (logs.isEmpty()) {
            logger.warn("No time logs found for user: {} between {} and {}", user.getEmail(), startDate, endDate);
        } else {
            logger.info("Retrieved {} time logs for user: {} between {} and {}", logs.size(), user.getEmail(), startDate, endDate);
        }
        return logs.stream()
                .map(log -> new TimeLogResponseDto(log.getDate(), log.getLoginTime(), log.getLogoutTime()))
                .collect(Collectors.toList());
    }

}
