package com.kloudspot.ems.dto;

import com.kloudspot.ems.model.Employee;
import com.kloudspot.ems.model.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeId,
        String fullName,
        String email,
        String phoneNumber,
        String department,
        String designation,
        LocalDate dateOfJoining,
        BigDecimal salary,
        EmploymentStatus status
) {

    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeId(),
                e.getFullName(),
                e.getEmail(),
                e.getPhoneNumber(),
                e.getDepartment(),
                e.getDesignation(),
                e.getDateOfJoining(),
                e.getSalary(),
                e.getStatus()
        );
    }
}
