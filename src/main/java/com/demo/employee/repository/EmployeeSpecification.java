package com.demo.employee.repository;

import com.demo.employee.model.Employee;
import com.demo.employee.model.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> hasName(String name) {
        return (root, query, cb) -> {
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("department")), department.toLowerCase());
    }

    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Employee> hasSalaryBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("salary"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("salary"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("salary"), max);
            }
        };
    }
}
