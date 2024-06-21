package de.atruvia.ase.samman.buli.domain.ports.primary;

import java.util.List;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.ddd.annotation.Service;

import de.atruvia.ase.samman.buli.domain.TabellenPlatz;

@Service
@PrimaryPort
public interface TabellenService {

	List<TabellenPlatz> erstelleTabelle(String league, String season);

}
