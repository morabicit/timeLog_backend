package com.example.itida.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogResponseDto {
    private LocalDate date;
    private LocalTime loginTime;
    private LocalTime logoutTime;
}
