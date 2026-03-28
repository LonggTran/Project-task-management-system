// seeds/LabelSeederService.java
package com.projecttaskmanager.backend.seeds;

import com.projecttaskmanager.backend.models.Label;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.repositories.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelSeederService {

    private final LabelRepository labelRepository;

    public void seedDefaultLabels(Project project) {
        if (labelRepository.findByProjectId(project.getId()).isEmpty()) {
            List<Label> defaultLabels = List.of(
                    Label.builder().name("bug").color("#ef4444").project(project).description("bug nè").build(),
                    Label.builder().name("feature").color("#3b82f6").project(project).description("chức năng mới nè").build(),
                    Label.builder().name("urgent").color("#f97316").project(project).description("urgent nè").build(),
                    Label.builder().name("improvement").color("#22c55e").project(project).description("phát triển tính năng nè").build()
            );

            labelRepository.saveAll(defaultLabels);
        }
    }
}