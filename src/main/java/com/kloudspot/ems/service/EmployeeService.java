package com.kloudspot.ems.service;

import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.exception.DuplicateResourceException;
import com.kloudspot.ems.exception.ResourceNotFoundException;
import com.kloudspot.ems.model.Employee;
import com.kloudspot.ems.model.EmploymentStatus;
import com.kloudspot.ems.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An employee with email '" + email + "' already exists");
        }

        Employee employee = new Employee();
        apply(employee, request);

        Employee saved = repository.save(employee);
        saved.setEmployeeId(formatEmployeeId(saved.getId()));
        saved = repository.save(saved);

        return EmployeeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findOrThrow(id);

        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Another employee with email '" + email + "' already exists");
        }

        apply(employee, request);
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findOrThrow(id);
        repository.delete(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(String search, String department,
                                         EmploymentStatus status, Pageable pageable) {
        return repository.findAll(buildSpecification(search, department, status), pageable)
                .map(EmployeeResponse::from);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAllForExport(String search, String department, EmploymentStatus status) {
        return repository.findAll(buildSpecification(search, department, status), Sort.by("id"))
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listDepartments() {
        return repository.findDistinctDepartments();
    }

    // ---------------------------------------------------------------- helpers

    private Employee findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    private void apply(Employee employee, EmployeeRequest r) {
        employee.setFullName(r.fullName().trim());
        employee.setEmail(normalizeEmail(r.email()));
        employee.setPhoneNumber(r.phoneNumber().trim());
        employee.setDepartment(r.department().trim());
        employee.setDesignation(r.designation().trim());
        employee.setDateOfJoining(r.dateOfJoining());
        employee.setSalary(r.salary());
        employee.setStatus(r.status());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    static String formatEmployeeId(Long id) {
        return String.format("EMP%04d", id);
    }

    static Specification<Employee> buildSpecification(String search, String department, EmploymentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
