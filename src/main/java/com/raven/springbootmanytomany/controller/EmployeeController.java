package com.raven.springbootmanytomany.controller;

import com.raven.springbootmanytomany.entity.Employee;
import com.raven.springbootmanytomany.entity.Project;
import com.raven.springbootmanytomany.repository.EmployeeRepository;
import com.raven.springbootmanytomany.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping("/createEmployee")
    public ResponseEntity<String> createEmployee(@RequestBody Employee employee) {
        logger.info("Creating a new Employee: {}", employee.getName());

        employeeRepository.save(employee);
        logger.info("Saved Employee: {}", employee.toString());

        return new ResponseEntity<>("Employee created successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/createEmployeeAndAssignProjects")
    public ResponseEntity<String> createEmployeeAndAssignProjects(@RequestBody CreateEmployeeWithProjectsRequest request) {
        logger.info("Creating a new Employee and assigning Projects: {}", request.getName());

        Employee employee = new Employee(request.getName(), request.getEmail(), request.getTechnicalSkill());

        List<Project> projects = projectRepository.findAllById(request.getProjectIds());
        if (projects.isEmpty()) {
            logger.warn("No valid Projects found for assignment.");
            return new ResponseEntity<>("No valid Projects found to assign.", HttpStatus.BAD_REQUEST);
        }

        employee.getProjects().addAll(projects);

        for (Project project : projects) {
            project.getEmployees().add(employee);
        }

        employeeRepository.save(employee);
        logger.info("Employee created with assigned projects: {}", employee.toString());

        return new ResponseEntity<>("Employee created and Projects assigned successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/assignProjectToEmployee/{employeeId}")
    public ResponseEntity<String> assignProjectToEmployee(
            @PathVariable Integer employeeId,
            @RequestBody Integer projectId) {
        logger.info("Assigning Project ID {} to Employee ID {}", projectId, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Employee ID: " + employeeId));
        logger.debug("Employee details: {}", employee.toString());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Project ID: " + projectId));
        logger.debug("Project details: {}", project.toString());

        if (!employee.getProjects().contains(project)) {
            employee.getProjects().add(project);
            project.getEmployees().add(employee);

            employeeRepository.save(employee);
            logger.info("Project ID {} assigned to Employee ID {}", projectId, employeeId);

            return new ResponseEntity<>("Project assigned to Employee successfully!", HttpStatus.OK);
        } else {
            logger.warn("Employee ID {} is already assigned to Project ID {}", employeeId, projectId);
            return new ResponseEntity<>("Employee is already assigned to this Project.", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/getEmployee/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Integer employeeId) {
        logger.info("Fetching Employee with ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Employee ID: " + employeeId));
        logger.debug("Employee details: {}", employee.toString());

        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    public static class CreateEmployeeWithProjectsRequest {
        private String name;
        private String email;
        private String technicalSkill;
        private List<Integer> projectIds;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTechnicalSkill() {
            return technicalSkill;
        }

        public void setTechnicalSkill(String technicalSkill) {
            this.technicalSkill = technicalSkill;
        }

        public List<Integer> getProjectIds() {
            return projectIds;
        }

        public void setProjectIds(List<Integer> projectIds) {
            this.projectIds = projectIds;
        }
    }
}
