package de.atruvia.ase.samman.buli.domain.ports.primary;

import java.util.List;

import org.jmolecules.architecture.hexagonal.PrimaryPort;

import de.atruvia.ase.samman.buli.domain.TabellenPlatz;

@PrimaryPort
public interface TabellenService {

	List<TabellenPlatz> erstelleTabelle(String league, String season);

}
