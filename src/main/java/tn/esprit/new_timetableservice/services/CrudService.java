package tn.esprit.new_timetableservice.services;

import java.util.List;

public interface CrudService<T, ID> {
    T create(T entity);
    T update(ID id, T entity);
    T findById(ID id);
    List<T> findAll();
    void delete(ID id);
}