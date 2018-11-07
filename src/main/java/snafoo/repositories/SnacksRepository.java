package snafoo.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import snafoo.utilities.Snacks;

public interface SnacksRepository extends CrudRepository<Snacks, Long> {
    List<Snacks> findAll();
    List<Snacks> findSnackById(int id);

}
