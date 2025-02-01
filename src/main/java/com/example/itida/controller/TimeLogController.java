package com.example.itida.controller;

import com.example.itida.dto.TimeLogResponseDto;
import com.example.itida.entity.TimeLog;
import com.example.itida.service.TimeLogService;
import jakarta.xml.bind.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class TimeLogController {

    private final Logger logger = LoggerFactory.getLogger(TimeLogController.class);
    private final TimeLogService timeLogService;

    @Autowired
    public TimeLogController(TimeLogService timeLogService) {
        this.timeLogService = timeLogService;
    }

    @GetMapping("/today")
    public ResponseEntity<TimeLogResponseDto> getTodayTimeLog() {
        TimeLogResponseDto timeLogResponseDto =  timeLogService.getTodayTimeLog();
        if (timeLogResponseDto == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(timeLogResponseDto, HttpStatus.OK);

    }
    @PostMapping("/submit")
    public ResponseEntity<TimeLogResponseDto> createTimeLog(@RequestBody TimeLog timeLog) throws ValidationException {
        logger.info("POST request / with current user and parameters: "+ timeLog);
        TimeLogResponseDto timeLogResponseDto = timeLogService.submitTimeLog(timeLog);
        return new ResponseEntity<>(timeLogResponseDto,HttpStatus.OK);
    }
    @GetMapping("/history")
    public ResponseEntity<List<TimeLogResponseDto>> getMonthlyTimeLogs(
            @RequestParam("month") String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TimeLogResponseDto> logs = timeLogService.getTimeLogsByUserAndDateRange(
                startDate,
                endDate
        );

        return ResponseEntity.ok(logs);
    }
}
