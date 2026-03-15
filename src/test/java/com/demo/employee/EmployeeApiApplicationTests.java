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
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void shouldGetAllEmployeesWithPagination() throws Exception {
        createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        createEmployeeInDb("Bob", "Jones", "bob@example.com", "Marketing");

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void shouldPaginateResults() throws Exception {
        for (int i = 0; i < 15; i++) {
            createEmployeeInDb("User" + i, "Last" + i, "user" + i + "@example.com", "Engineering");
        }

        mockMvc.perform(get("/api/employees").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
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

    @Test
    void shouldSearchByName() throws Exception {
        createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        createEmployeeInDb("Bob", "Jones", "bob@example.com", "Marketing");

        mockMvc.perform(get("/api/employees/search").param("name", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    @Test
    void shouldSearchByDepartmentAndStatus() throws Exception {
        Employee emp = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        createEmployeeInDb("Bob", "Jones", "bob@example.com", "Marketing");
        createEmployeeInDb("Carol", "White", "carol@example.com", "Engineering");

        // Change Alice's status
        emp.setStatus(EmployeeStatus.ON_LEAVE);
        repository.save(emp);

        mockMvc.perform(get("/api/employees/search")
                        .param("department", "Engineering")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Carol"));
    }

    @Test
    void shouldSearchBySalaryRange() throws Exception {
        Employee low = createEmployeeInDb("Alice", "Smith", "alice@example.com", "Engineering");
        low.setSalary(30000.0);
        repository.save(low);

        Employee high = createEmployeeInDb("Bob", "Jones", "bob@example.com", "Engineering");
        high.setSalary(90000.0);
        repository.save(high);

        mockMvc.perform(get("/api/employees/search")
                        .param("minSalary", "50000")
                        .param("maxSalary", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Bob"));
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
