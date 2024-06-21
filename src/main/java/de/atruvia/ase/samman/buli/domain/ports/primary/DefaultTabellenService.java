package de.atruvia.ase.samman.buli.domain.ports.primary;

import java.util.List;

import org.springframework.stereotype.Service;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import de.atruvia.ase.samman.buli.domain.ports.secondary.SpieltagRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultTabellenService implements TabellenService {

	private final SpieltagRepo spieltagRepo;

	@Override
	public List<TabellenPlatz> erstelleTabelle(String league, String season) {
		Tabelle tabelle = new Tabelle();
		lade(league, season).forEach(tabelle::add);
		return tabelle.getEntries();
	}

	private List<Paarung> lade(String league, String season) {
		return spieltagRepo.lade(league, season);
	}

}
