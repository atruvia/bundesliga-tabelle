package de.atruvia.ase.samman.buli;

import de.atruvia.ase.sammanbuli.domain.ports.primary.DefaultTabellenService;
import de.atruvia.ase.sammanbuli.domain.ports.primary.TabellenService;
import de.atruvia.ase.sammanbuli.domain.ports.secondary.SpieltagRepo;
import de.atruvia.ase.sammanbuli.infra.adapters.secondary.OpenLigaDbSpieltagRepo;

public class Main {
	
	public static void main(String[] args) {
		SpieltagRepo r = new OpenLigaDbSpieltagRepo();
		TabellenService tabellenService = new DefaultTabellenService();
		
		
	}

}
