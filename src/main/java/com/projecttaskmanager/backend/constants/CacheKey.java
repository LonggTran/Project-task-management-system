package com.projecttaskmanager.backend.constants;

import java.util.UUID;

public class CacheKey {

    public static String userProjects(String email) {
        return "projects:user:" + email;
    }

    public static String projectDetail(UUID id) {
        return "project:id:" + id;
    }

    public static String userMe(String email) {
        return "user:me:" + email;
    }

    public static String epicDetail(UUID id) {
        return "epic:id:" + id;
    }

    public static String epicsByProject(UUID projectId) {
        return "epics:project:" + projectId;
    }

    public static String taskDetail(UUID taskId) {
        return "task:id:" + taskId;
    }

    public static String tasksByProject(UUID projectId) {
        return "tasks:project:" + projectId;
    }

    public static String taskLabels(UUID taskId) {
        return "task:labels:" + taskId;
    }

    public static String subtasksByParent(UUID parentTaskId) {
        return "subtasks:parent:" + parentTaskId;
    }

    public static String subtaskTree(UUID taskId) {
        return "subtasks:tree:" + taskId;
    }

    public static String sprintsByProject(UUID projectId) {
        return "sprints:project:" + projectId;
    }

    public static String sprintTasks(UUID sprintId) {
        return "sprint:tasks:" + sprintId;
    }

    public static String sprintDetail(UUID sprintId) {
        return "sprint:id:" + sprintId;
    }
}