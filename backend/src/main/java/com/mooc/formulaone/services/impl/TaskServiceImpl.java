package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.TaskRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.services.TaskService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implementation de {@link TaskService} responsable des operations CRUD
 * sur les taches du domaine.
 */
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Retourne l'ensemble des taches persistees.
     *
     * @return la liste complete des taches
     */
    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        taskRepository.findAll().forEach(tasks::add);
        return tasks;
    }

    /**
     * Recherche une tache par identifiant ou leve une exception 404 metier
     * lorsque la tache n'existe pas.
     *
     * @param id identifiant de la tache
     * @return la tache correspondante
     */
    @Override
    public Task findById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            return task.get();
        }
        throw new EntityDontExistException();
    }

    /**
     * Persiste une nouvelle tache.
     *
     * @param task tache a enregistrer
     * @return identifiant genere
     */
    @Override
    public Long create(Task task) {
        return taskRepository.save(task).getId();
    }

    /**
     * Supprime la tache fournie.
     *
     * @param task tache a supprimer
     */
    @Override
    public void delete(Task task) {
        taskRepository.delete(task);
    }
}
