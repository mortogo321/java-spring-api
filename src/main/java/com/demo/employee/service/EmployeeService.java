package com.demo.employee.service;

import com.demo.employee.dto.EmployeeRequest;
import com.demo.employee.exception.DuplicateResourceException;
import com.demo.employee.exception.ResourceNotFoundException;
import com.demo.employee.model.Employee;
import com.demo.employee.model.EmployeeStatus;
import com.demo.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Employee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public List<Employee> findByDepartment(String department) {
        return repository.findByDepartment(department);
    }

    @Transactional
    public Employee create(EmployeeRequest request) {
        repository.findByEmail(request.getEmail()).ifPresent(e -> {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        });

        Employee employee = new Employee();
        mapRequestToEmployee(request, employee);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return repository.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = findById(id);

        repository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(e -> {
                    throw new DuplicateResourceException("Email already exists: " + request.getEmail());
                });

        mapRequestToEmployee(request, employee);
        return repository.save(employee);
    }

    @Transactional
    public Employee updateStatus(Long id, EmployeeStatus status) {
        Employee employee = findById(id);
        employee.setStatus(status);
        return repository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findById(id);
        repository.delete(employee);
    }

    public Map<String, Long> getDepartmentStats() {
        return repository.countByDepartment().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    private void mapRequestToEmployee(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
    }
}
