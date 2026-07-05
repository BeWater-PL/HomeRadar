package de.bewater.homeradar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.bewater.homeradar.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

	Optional<Person> findByName(String name);

	/** Laedt alle Personen samt ihrer Geraete, um Lazy-Loading zu vermeiden. */
	@Query("select distinct p from Person p left join fetch p.geraete")
	List<Person> findAllWithGeraete();
}
