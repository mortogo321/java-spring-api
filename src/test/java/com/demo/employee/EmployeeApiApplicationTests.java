package com.demo.employee;

import com.demo.employee.dto.EmployeeRequest;
import com.demo.employee.model.Employee;
import com.demo.employee.model.EmployeeStatus;
import com.demo.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeRequest request = createRequest("John", "Doe", "john@example.com", "Engineering");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        createEmployeeInDb("Bob", "Jones", "bob@example.com", "Marketing");

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void shouldReturn404ForMissingEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        EmployeeRequest update = createRequest("Alice", "Johnson", "alice@example.com", "Management");

        mockMvc.perform(put("/api/employees/" + emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.department").value("Management"));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        EmployeeRequest request = createRequest("Bob", "Jones", "alice@example.com", "Marketing");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldValidateRequest() throws Exception {
        EmployeeRequest request = new EmployeeRequest(); // all fields blank

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");

        mockMvc.perform(delete("/api/employees/" + emp.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");

        mockMvc.perform(patch("/api/employees/" + emp.getId() + "/status")
                        .param("status", "ON_LEAVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));
    }

    @Test
    void shouldGetByDepartment() throws Exception {
        createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        createEmployeeInDb("Bob", "Jones", "bob@example.com", "Marketing");
        createEmployeeInDb("Carol", "White", "carol@example.com", "Engineering");

        mockMvc.perform(get("/api/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private EmployeeRequest createRequest(String first, String last, String email, String dept) {
        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName(first);
        req.setLastName(last);
        req.setEmail(email);
        req.setDepartment(dept);
        req.setSalary(50000.0);
        req.setHireDate(LocalDate.now());
        return req;
    }

    private Employee createEmployeeInDb(String first, String last, String email, String dept) {
        Employee emp = new Employee(first, last, email, dept);
        emp.setSalary(50000.0);
        emp.setHireDate(LocalDate.now());
        emp.setStatus(EmployeeStatus.ACTIVE);
        return repository.save(emp);
    }
}
