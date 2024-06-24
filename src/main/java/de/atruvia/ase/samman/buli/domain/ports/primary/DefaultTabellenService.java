package de.atruvia.ase.samman.buli.domain.ports.primary;

import java.util.List;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.ports.secondary.SpieltagRepo;
import de.atruvia.ase.samman.buli.springframework.PrimaryPortImplementation;
import lombok.RequiredArgsConstructor;

@PrimaryPortImplementation(TabellenService.class)
@RequiredArgsConstructor
public class DefaultTabellenService implements TabellenService {

	private final SpieltagRepo spieltagRepo;

	@Override
	public Tabelle erstelleTabelle(String league, String season) {
		Tabelle tabelle = new Tabelle();
		lade(league, season).forEach(tabelle::add);
		return tabelle;
	}

	private List<Paarung> lade(String league, String season) {
		return spieltagRepo.lade(league, season);
	}

}
