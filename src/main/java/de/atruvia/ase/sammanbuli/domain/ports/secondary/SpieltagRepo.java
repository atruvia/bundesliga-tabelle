package de.atruvia.ase.sammanbuli.domain.ports.secondary;

import java.util.List;

import de.atruvia.ase.sammanbuli.domain.Paarung;

public interface SpieltagRepo {

	List<Paarung> lade(String league, String season) throws Exception;

}
