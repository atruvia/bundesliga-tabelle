package de.atruvia.ase.samman.buli.domain.ports.secondary;

import java.util.List;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import de.atruvia.ase.samman.buli.domain.Paarung;

@SecondaryPort
@Repository
public interface SpieltagRepo {

	List<Paarung> lade(String league, String season);

}
