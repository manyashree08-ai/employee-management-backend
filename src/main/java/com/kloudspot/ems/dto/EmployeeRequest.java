package com.kloudspot.ems.dto;

import com.kloudspot.ems.model.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload used to create or update an employee")
public record EmployeeRequest(

        @Schema(example = "Aarav Sharma")
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName,

        @Schema(example = "aarav.sharma@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Schema(example = "+91 98765 43210")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[+]?[0-9\\-\\s()]{7,20}$",
                message = "Phone number must contain 7-20 digits, spaces, dashes, brackets or a leading +")
        String phoneNumber,

        @Schema(example = "Engineering")
        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must be at most 100 characters")
        String department,

        @Schema(example = "Software Engineer")
        @NotBlank(message = "Designation is required")
        @Size(max = 100, message = "Designation must be at most 100 characters")
        String designation,

        @Schema(example = "2024-07-01", type = "string", format = "date")
        @NotNull(message = "Date of joining is required")
        LocalDate dateOfJoining,

        @Schema(example = "850000.00")
        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.0", message = "Salary cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Salary can have at most 10 integer digits and 2 decimals")
        BigDecimal salary,

        @Schema(example = "ACTIVE")
        @NotNull(message = "Employment status is required (ACTIVE or INACTIVE)")
        EmploymentStatus status
) {
}
