package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Project;
import com.dootask.backend.service.ProjectService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Tag(name = "项目管理", description = "项目相关接口")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @Operation(summary = "获取项目列表")
    @GetMapping
    public Result<List<Project>> getProjects(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> projects = projectService.getProjectsByUser(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Project project = projectService.getProjectById(id, userId);
        return Result.success(project);
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public Result<Project> createProject(@Valid @RequestBody CreateProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        Project project = new Project();
        project.setName(request.getName());
        project.setDesc(request.getDesc());

        Project createdProject = projectService.createProject(project, userId);
        return Result.success("创建成功", createdProject);
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        Project project = new Project();
        project.setName(request.getName());
        project.setDesc(request.getDesc());

        Project updatedProject = projectService.updateProject(id, project, userId);
        return Result.success("更新成功", updatedProject);
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.deleteProject(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "置顶项目")
    @PostMapping("/{id}/top")
    public Result<Void> topProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.topProject(id, userId);
        return Result.success("操作成功");
    }

    @Operation(summary = "归档项目")
    @PostMapping("/{id}/archive")
    public Result<Void> archiveProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.archiveProject(id, userId);
        return Result.success("归档成功");
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // 从JWT token中获取用户ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }

    public static class CreateProjectRequest {
        @NotBlank(message = "项目名称不能为空")
        private String name;

        private String desc;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    public static class UpdateProjectRequest {
        @NotBlank(message = "项目名称不能为空")
        private String name;

        private String desc;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }
}