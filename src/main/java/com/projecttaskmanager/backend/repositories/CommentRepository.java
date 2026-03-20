package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Comment;
import com.projecttaskmanager.backend.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByTask(Task task);
}