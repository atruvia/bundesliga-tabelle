package de.atruvia.ase.samman.buli.domain.ports.primary;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.ddd.annotation.Service;

import de.atruvia.ase.samman.buli.domain.Tabelle;

@Service
@PrimaryPort
public interface TabellenService {

	Tabelle erstelleTabelle(String league, String season);

}
