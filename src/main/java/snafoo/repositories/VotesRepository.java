package snafoo.repositories;

import org.springframework.data.repository.CrudRepository;
import snafoo.utilities.Votes;

import java.util.List;

public interface VotesRepository extends CrudRepository<Votes, Long> {
    List<Votes> findAll();
}
