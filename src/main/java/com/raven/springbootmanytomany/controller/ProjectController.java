package com.raven.springbootmanytomany.controller;

import com.raven.springbootmanytomany.entity.Project;
import com.raven.springbootmanytomany.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping("/createProject")
    public ResponseEntity<String> createProject(@RequestBody Project project) {
        logger.info("Creating a new Project: {}", project.getProjectName());

        Project newProject = new Project(project.getProjectName(), project.getTechnologyUsed());
        projectRepository.save(newProject);

        logger.info("Saved Project: {}", newProject.toString());
        return new ResponseEntity<>("Project created successfully!", HttpStatus.CREATED);
    }

    @GetMapping("/getProject/{projId}")
    public ResponseEntity<Project> getProject(@PathVariable Integer projId) {
        logger.info("Fetching Project with ID: {}", projId);

        Project project = projectRepository.findById(projId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projId));

        logger.debug("Project details: {}", project.toString());
        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
