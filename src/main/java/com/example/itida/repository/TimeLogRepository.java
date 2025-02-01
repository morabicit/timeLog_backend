package com.example.itida.repository;

import com.example.itida.entity.TimeLog;
import com.example.itida.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    Optional<TimeLog> findByUserAndDate(User user, LocalDate date);
    List<TimeLog> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

}
