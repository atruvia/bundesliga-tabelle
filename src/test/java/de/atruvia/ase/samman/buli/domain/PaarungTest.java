package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.domain.PaarungMother.paarungWithAllAttributesSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;

class PaarungTest {

	@Test
	void heimUndAuswaertsView() {
		var paarung = paarungWithAllAttributesSet();
		var heim = paarung.viewForTeam(HEIM);
		var ausw = paarung.viewForTeam(AUSWAERTS);

		assertEqualsViceVersa(heim, ausw, PaarungView::team, PaarungView::gegner);
		assertEqualsViceVersa(heim, ausw, PaarungView::tore, PaarungView::gegentore);

		assertThat(heim.ergebnisTyp()).isEqualTo(ausw.ergebnisTyp());

		assertThat(heim.direction()).isEqualTo(HEIM);
		assertThat(heim.ergebnis()).isEqualTo(NIEDERLAGE);

		assertThat(ausw.direction()).isEqualTo(AUSWAERTS);
		assertThat(ausw.ergebnis()).isEqualTo(SIEG);
	}

	private static void assertEqualsViceVersa(PaarungView view1, PaarungView view2, Function<PaarungView, Object> f1,
			Function<PaarungView, Object> f2) {
		assertEquals(f1.apply(view1), f2.apply(view2));
		assertEquals(f1.apply(view2), f2.apply(view1));
	}

}
