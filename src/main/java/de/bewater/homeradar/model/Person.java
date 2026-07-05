package de.bewater.homeradar.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eine Person, der ein oder mehrere Geraete zugeordnet sein koennen.
 * Ueber die Geraete wird die Anwesenheit abgeleitet.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Person {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@OneToMany(mappedBy = "person")
	private List<Geraet> geraete = new ArrayList<>();
}
