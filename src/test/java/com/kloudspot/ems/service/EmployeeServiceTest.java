package com.kloudspot.ems.service;

import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.exception.DuplicateResourceException;
import com.kloudspot.ems.exception.ResourceNotFoundException;
import com.kloudspot.ems.model.Employee;
import com.kloudspot.ems.model.EmploymentStatus;
import com.kloudspot.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    // ------------------------------------------------------------ fixtures

    private EmployeeRequest request(String email) {
        return new EmployeeRequest(
                "  Aarav Sharma  ",
                email,
                "+91 98765 43210",
                "Engineering",
                "Software Engineer",
                LocalDate.of(2024, 7, 1),
                new BigDecimal("850000.00"),
                EmploymentStatus.ACTIVE);
    }

    private Employee existing(Long id, String email) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeId(String.format("EMP%04d", id));
        e.setFullName("Old Name");
        e.setEmail(email);
        e.setPhoneNumber("1234567");
        e.setDepartment("Finance");
        e.setDesignation("Analyst");
        e.setDateOfJoining(LocalDate.of(2020, 1, 1));
        e.setSalary(new BigDecimal("500000"));
        e.setStatus(EmploymentStatus.INACTIVE);
        return e;
    }

    // -------------------------------------------------------------- create

    @Test
    @DisplayName("create: saves employee, normalises input and generates the employee ID")
    void create_success() {
        when(repository.existsByEmailIgnoreCase("aarav.sharma@example.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(7L);
            }
            return e;
        });

        EmployeeResponse response = service.create(request("  Aarav.Sharma@Example.com "));

        assertEquals(7L, response.id());
        assertEquals("EMP0007", response.employeeId());
        assertEquals("Aarav Sharma", response.fullName());
        assertEquals("aarav.sharma@example.com", response.email());
        assertEquals(EmploymentStatus.ACTIVE, response.status());
        // first save to obtain the id, second to store the generated employee ID
        verify(repository, times(2)).save(any(Employee.class));
    }

    @Test
    @DisplayName("create: rejects a duplicate email")
    void create_duplicateEmail_throws() {
        when(repository.existsByEmailIgnoreCase("aarav.sharma@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.create(request("aarav.sharma@example.com")));

        verify(repository, never()).save(any(Employee.class));
    }

    // ------------------------------------------------------------- getById

    @Test
    @DisplayName("getById: returns the employee when present")
    void getById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L, "a@example.com")));

        EmployeeResponse response = service.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("EMP0001", response.employeeId());
        assertEquals("a@example.com", response.email());
    }

    @Test
    @DisplayName("getById: throws when the employee does not exist")
    void getById_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // -------------------------------------------------------------- update

    @Test
    @DisplayName("update: applies all fields to the existing employee")
    void update_success() {
        Employee entity = existing(1L, "old@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = service.update(1L, request("New@Example.com"));

        assertEquals("Aarav Sharma", response.fullName());
        assertEquals("new@example.com", response.email());
        assertEquals("Engineering", response.department());
        assertEquals(new BigDecimal("850000.00"), response.salary());
        assertEquals(EmploymentStatus.ACTIVE, response.status());
        // the generated business ID must never change on update
        assertEquals("EMP0001", response.employeeId());
    }

    @Test
    @DisplayName("update: rejects an email that belongs to another employee")
    void update_duplicateEmail_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing(1L, "old@example.com")));
        when(repository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.update(1L, request("taken@example.com")));

        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("update: throws when the employee does not exist")
    void update_notFound_throws() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(5L, request("x@example.com")));

        verify(repository, never()).save(any(Employee.class));
    }

    // -------------------------------------------------------------- delete

    @Test
    @DisplayName("delete: removes an existing employee")
    void delete_success() {
        Employee entity = existing(3L, "c@example.com");
        when(repository.findById(3L)).thenReturn(Optional.of(entity));

        service.delete(3L);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete: throws when the employee does not exist")
    void delete_notFound_throws() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(3L));

        verify(repository, never()).delete(any(Employee.class));
    }

    // -------------------------------------------------------------- search

    @Test
    @DisplayName("search: delegates to the repository with the pageable and maps the results")
    void search_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Employee> page = new PageImpl<>(
                List.of(existing(1L, "a@example.com"), existing(2L, "b@example.com")), pageable, 2);
        when(repository.findAll(ArgumentMatchers.<Specification<Employee>>any(), eq(pageable))).thenReturn(page);

        Page<EmployeeResponse> result = service.search("a", "Finance", EmploymentStatus.INACTIVE, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("a@example.com", result.getContent().get(0).email());
        assertEquals("b@example.com", result.getContent().get(1).email());
    }

    @Test
    @DisplayName("search: works with no filters at all")
    void search_noFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        when(repository.findAll(ArgumentMatchers.<Specification<Employee>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<EmployeeResponse> result = service.search(null, null, null, pageable);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------- export / departments

    @Test
    @DisplayName("findAllForExport: returns every match ordered by id")
    void findAllForExport_sortedById() {
        when(repository.findAll(ArgumentMatchers.<Specification<Employee>>any(), any(Sort.class)))
                .thenReturn(List.of(existing(1L, "a@example.com")));

        List<EmployeeResponse> result = service.findAllForExport(null, "Finance", null);

        assertEquals(1, result.size());
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<Employee>>any(), sortCaptor.capture());
        assertEquals(Sort.by("id"), sortCaptor.getValue());
    }

    @Test
    @DisplayName("listDepartments: returns the distinct department names")
    void listDepartments() {
        when(repository.findDistinctDepartments()).thenReturn(List.of("Engineering", "Finance"));

        assertEquals(List.of("Engineering", "Finance"), service.listDepartments());
    }

    @Test
    @DisplayName("formatEmployeeId: pads the numeric id to four digits")
    void formatEmployeeId() {
        assertEquals("EMP0001", EmployeeService.formatEmployeeId(1L));
        assertEquals("EMP0123", EmployeeService.formatEmployeeId(123L));
        assertEquals("EMP12345", EmployeeService.formatEmployeeId(12345L));
    }
}
