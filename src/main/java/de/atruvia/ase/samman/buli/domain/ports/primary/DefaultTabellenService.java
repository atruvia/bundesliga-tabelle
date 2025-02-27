package de.atruvia.ase.samman.buli.domain.ports.primary;

import java.util.List;

import de.atruvia.ase.samman.buli.domain.DefaultTabelle;
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
		List<Paarung> paarungen = spieltagRepo.lade(league, season);
		return accumulateToTabelle(paarungen);
	}

	private static Tabelle accumulateToTabelle(List<Paarung> paarungen) {
		Tabelle tabelle = new DefaultTabelle();
		for (Paarung paarung : paarungen) {
			tabelle = tabelle.add(paarung);
		}
		return tabelle;
	}

}
