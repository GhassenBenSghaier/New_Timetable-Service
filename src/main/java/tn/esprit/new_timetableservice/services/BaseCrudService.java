package tn.esprit.new_timetableservice.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public abstract class BaseCrudService<T, ID> implements CrudService<T, ID> {
    protected final JpaRepository<T, ID> repository;

    public BaseCrudService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public T create(T entity) {
        return repository.save(entity);
    }

    @Override
    public T update(ID id, T entity) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }
        return repository.save(entity);
    }

    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }
        repository.deleteById(id);
    }
}