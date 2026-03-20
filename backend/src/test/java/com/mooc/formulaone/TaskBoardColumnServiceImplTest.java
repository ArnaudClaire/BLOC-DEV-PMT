package com.mooc.formulaone;

import com.mooc.formulaone.dao.TaskBoardColumnRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.services.impl.TaskBoardColumnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskBoardColumnServiceImplTest {

    @Mock
    private TaskBoardColumnRepository taskBoardColumnRepository;

    private TaskBoardColumnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TaskBoardColumnServiceImpl(taskBoardColumnRepository);
    }

    @Test
    void shouldSortColumnsByProjectThenDisplayOrderThenId() {
        Project project = new Project();
        project.setId(2L);

        TaskBoardColumn first = new TaskBoardColumn();
        first.setId(3L);
        first.setName("DONE");
        first.setProject(project);
        first.setDisplayOrder(2);

        TaskBoardColumn second = new TaskBoardColumn();
        second.setId(2L);
        second.setName("TODO");
        second.setProject(project);
        second.setDisplayOrder(0);

        TaskBoardColumn third = new TaskBoardColumn();
        third.setId(1L);
        third.setName("BACKLOG");
        third.setProject(null);
        third.setDisplayOrder(null);

        when(taskBoardColumnRepository.findAll()).thenReturn(List.of(first, third, second));

        List<TaskBoardColumn> columns = service.findAll();

        assertThat(columns).extracting(TaskBoardColumn::getId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void shouldThrowWhenColumnDoesNotExist() {
        when(taskBoardColumnRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(77L))
                .isInstanceOf(EntityDontExistException.class);
    }
}
