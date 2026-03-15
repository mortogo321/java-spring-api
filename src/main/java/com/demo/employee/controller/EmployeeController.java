package com.demo.employee.controller;

import com.demo.employee.dto.EmployeeRequest;
import com.demo.employee.dto.EmployeeResponse;
import com.demo.employee.model.Employee;
import com.demo.employee.model.EmployeeStatus;
import com.demo.employee.repository.EmployeeSpecification;
import com.demo.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all employees with pagination")
    public Page<EmployeeResponse> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return service.findAll(pageable).map(EmployeeResponse::fromEntity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public EmployeeResponse getById(@PathVariable Long id) {
        return EmployeeResponse.fromEntity(service.findById(id));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get employees by department")
    public List<EmployeeResponse> getByDepartment(@PathVariable String department) {
        return service.findByDepartment(department).stream()
                .map(EmployeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/stats/departments")
    @Operation(summary = "Get employee count per department")
    public Map<String, Long> getDepartmentStats() {
        return service.getDepartmentStats();
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees with filters")
    public Page<EmployeeResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Specification<Employee> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and(EmployeeSpecification.hasName(name));
        }
        if (department != null && !department.isBlank()) {
            spec = spec.and(EmployeeSpecification.hasDepartment(department));
        }
        if (status != null) {
            spec = spec.and(EmployeeSpecification.hasStatus(status));
        }
        if (minSalary != null || maxSalary != null) {
            spec = spec.and(EmployeeSpecification.hasSalaryBetween(minSalary, maxSalary));
        }

        return service.search(spec, pageable).map(EmployeeResponse::fromEntity);
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        Employee created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return EmployeeResponse.fromEntity(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update employee status")
    public EmployeeResponse updateStatus(@PathVariable Long id, @RequestParam EmployeeStatus status) {
        return EmployeeResponse.fromEntity(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
