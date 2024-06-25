package de.atruvia.ase.samman.buli.domain;

import java.util.List;

public interface Tabelle {

	Tabelle add(Paarung paarung);

	List<TabellenPlatz> getEntries();

}