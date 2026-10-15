package com.kloudspot.ems.controller;

import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.model.EmploymentStatus;
import com.kloudspot.ems.service.EmployeeService;
import com.kloudspot.ems.util.CsvExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee management APIs")
public class EmployeeController {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "employeeId", "fullName", "email", "department",
            "designation", "dateOfJoining", "salary", "status");

    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
    }

    @Operation(summary = "List employees",
            description = "Paginated list. Supports search by name/email and filters by department and status.")
    @GetMapping
    public PageResponse<EmployeeResponse> list(
            @Parameter(description = "Search text matched against full name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Exact department name (case-insensitive)")
            @RequestParam(required = false) String department,
            @Parameter(description = "ACTIVE or INACTIVE")
            @RequestParam(required = false) EmploymentStatus status,
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: id, employeeId, fullName, email, department, designation, dateOfJoining, salary, status")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "asc or desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Cannot sort by '" + sortBy + "'. Allowed: " + SORTABLE_FIELDS);
        }
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);

        return PageResponse.from(service.search(search, department, status, pageable));
    }

    @Operation(summary = "Get employee details by id")
    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Update an employee")
    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete an employee")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @Operation(summary = "List distinct departments", description = "Used to populate department filters/dropdowns.")
    @GetMapping("/departments")
    public List<String> departments() {
        return service.listDepartments();
    }

    @Operation(summary = "Export employees to CSV",
            description = "Exports all employees matching the optional search and filters.")
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmploymentStatus status) {

        String csv = CsvExporter.toCsv(service.findAllForExport(search, department, status));
        // UTF-8 BOM so Excel opens non-ASCII names correctly
        byte[] body = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }
}
