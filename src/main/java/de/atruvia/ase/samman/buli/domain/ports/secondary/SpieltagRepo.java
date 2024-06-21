package de.atruvia.ase.samman.buli.domain.ports.secondary;

import java.util.List;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

import de.atruvia.ase.samman.buli.domain.Paarung;

@SecondaryPort
public interface SpieltagRepo {

	List<Paarung> lade(String league, String season);

}
