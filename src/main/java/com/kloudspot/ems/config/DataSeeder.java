package com.kloudspot.ems.config;

import com.kloudspot.ems.model.Employee;
import com.kloudspot.ems.model.EmploymentStatus;
import com.kloudspot.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inserts demo data on first start (only when the employees table is empty).
 * Disable with APP_SEED_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final EmployeeRepository repository;

    public DataSeeder(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        add("Aarav Sharma", "aarav.sharma@example.com", "+91 98765 43210", "Engineering", "Senior Software Engineer", "2021-03-15", "1800000", EmploymentStatus.ACTIVE);
        add("Priya Nair", "priya.nair@example.com", "+91 98765 43211", "Engineering", "Backend Developer", "2022-07-01", "1200000", EmploymentStatus.ACTIVE);
        add("Rohan Mehta", "rohan.mehta@example.com", "+91 98765 43212", "Engineering", "QA Engineer", "2023-01-09", "900000", EmploymentStatus.ACTIVE);
        add("Sneha Iyer", "sneha.iyer@example.com", "+91 98765 43213", "Human Resources", "HR Manager", "2019-11-20", "1500000", EmploymentStatus.ACTIVE);
        add("Vikram Singh", "vikram.singh@example.com", "+91 98765 43214", "Finance", "Financial Analyst", "2020-05-18", "1100000", EmploymentStatus.ACTIVE);
        add("Ananya Reddy", "ananya.reddy@example.com", "+91 98765 43215", "Marketing", "Marketing Executive", "2022-02-14", "800000", EmploymentStatus.ACTIVE);
        add("Karthik Rao", "karthik.rao@example.com", "+91 98765 43216", "Engineering", "DevOps Engineer", "2021-09-06", "1400000", EmploymentStatus.INACTIVE);
        add("Meera Pillai", "meera.pillai@example.com", "+91 98765 43217", "Sales", "Sales Manager", "2018-06-25", "1600000", EmploymentStatus.ACTIVE);
        add("Arjun Verma", "arjun.verma@example.com", "+91 98765 43218", "Sales", "Account Executive", "2023-04-03", "750000", EmploymentStatus.ACTIVE);
        add("Divya Menon", "divya.menon@example.com", "+91 98765 43219", "Finance", "Accountant", "2020-12-01", "850000", EmploymentStatus.INACTIVE);
        add("Rahul Gupta", "rahul.gupta@example.com", "+91 98765 43220", "Marketing", "Content Strategist", "2021-08-16", "950000", EmploymentStatus.ACTIVE);
        add("Neha Kapoor", "neha.kapoor@example.com", "+91 98765 43221", "Human Resources", "Recruiter", "2022-10-10", "700000", EmploymentStatus.ACTIVE);
        add("Siddharth Joshi", "siddharth.joshi@example.com", "+91 98765 43222", "Engineering", "Software Engineer", "2024-06-17", "1000000", EmploymentStatus.ACTIVE);
        add("Pooja Bhat", "pooja.bhat@example.com", "+91 98765 43223", "Operations", "Operations Lead", "2019-03-04", "1300000", EmploymentStatus.ACTIVE);
        add("Aditya Kulkarni", "aditya.kulkarni@example.com", "+91 98765 43224", "Operations", "Logistics Coordinator", "2023-09-11", "650000", EmploymentStatus.INACTIVE);

        log.info("Seeded {} demo employees", repository.count());
    }

    private void add(String name, String email, String phone, String dept, String designation,
                     String joined, String salary, EmploymentStatus status) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setPhoneNumber(phone);
        e.setDepartment(dept);
        e.setDesignation(designation);
        e.setDateOfJoining(LocalDate.parse(joined));
        e.setSalary(new BigDecimal(salary));
        e.setStatus(status);
        e = repository.save(e);
        e.setEmployeeId(String.format("EMP%04d", e.getId()));
        repository.save(e);
    }
}
