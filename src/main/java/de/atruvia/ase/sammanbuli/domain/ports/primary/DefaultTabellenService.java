package de.atruvia.ase.sammanbuli.domain.ports.primary;

import de.atruvia.ase.sammanbuli.domain.Tabelle;
import de.atruvia.ase.sammanbuli.domain.ports.secondary.SpieltagRepo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultTabellenService implements TabellenService {

	private final SpieltagRepo spieltagRepo;

	@Override
	public void erstelleTabelle(String league, String season) {
		Tabelle tabelle = new Tabelle();
		
		spieltagRepo.l
		
		tabelle.add(p);
		
		
		// TODO Auto-generated method stub

	}

}
