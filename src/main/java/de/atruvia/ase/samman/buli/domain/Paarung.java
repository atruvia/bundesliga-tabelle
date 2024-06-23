package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.GEPLANT;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.LAUFEND;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;

import java.util.function.Function;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;

@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ValueObject
public class Paarung {

	@Value
	public class PaarungView {

		ViewDirection direction;
		Function<Paarung, Entry> team;
		Function<Paarung, Entry> gegner;
		@Getter(lazy = true)
		Ergebnis ergebnis = calcErgebnis();

		public Entry team() {
			return team.apply(paarung());
		}

		public Entry gegner() {
			return gegner.apply(paarung());
		}

		public Tore tore() {
			return team().tore();
		}

		public Tore gegentore() {
			return gegner().tore();
		}

		public boolean isGeplant() {
			return paarung().isGeplant();
		}

		public boolean isLaufend() {
			return paarung().isLaufend();
		}

		private Ergebnis calcErgebnis() {
			int compared = tore().compareTo(gegentore());
			if (compared == 0) {
				return UNENTSCHIEDEN;
			} else if (compared > 0) {
				return SIEG;
			} else {
				return NIEDERLAGE;
			}
		}

		public ErgebnisTyp ergebnisTyp() {
			return paarung().ergebnisTyp;
		}

		private Paarung paarung() {
			return Paarung.this;
		}

	}

	public enum ViewDirection {
		HEIM, AUSWAERTS
	}

	@RequiredArgsConstructor
	@Getter
	@Accessors(fluent = true)
	public enum Ergebnis {

		SIEG, UNENTSCHIEDEN, NIEDERLAGE;

		public int punkte() {
			return switch (this) {
			case SIEG -> 3;
			case UNENTSCHIEDEN -> 1;
			case NIEDERLAGE -> 0;
			};
		}

	}

	public enum ErgebnisTyp {
		GEPLANT, LAUFEND, BEENDET
	}

	@Value
	@RequiredArgsConstructor
	@Builder(toBuilder = true)
	public static class Entry {
		Team team;
		@With
		Tore tore;
	}

	@Builder.Default
	ErgebnisTyp ergebnisTyp = GEPLANT;

	Entry heim, gast;

	public boolean isGeplant() {
		return ergebnisTypIs(GEPLANT);
	}

	public boolean isLaufend() {
		return ergebnisTypIs(LAUFEND);
	}

	private boolean ergebnisTypIs(ErgebnisTyp ergebnisTyp) {
		return this.ergebnisTyp == ergebnisTyp;
	}

	public Paarung withErgebnis(Tore toreHeim, Tore toreGast) {
		return toBuilder().endergebnis(toreHeim, toreGast).build();
	}

	public ViewDirection viewDirection() {
		return HEIM;
	}

	public static class PaarungBuilder {

		public static PaarungBuilder paarung(String heim, String gast) {
			return Paarung.builder().heim(entry(heim)).gast(entry(gast));
		}

		private static Entry entry(String team) {
			return Entry.builder().team(Team.builder().name(team).build()).build();
		}

		public PaarungBuilder endergebnis(Tore toreHeim, Tore toreGast) {
			return ergebnis(BEENDET, toreHeim, toreGast);
		}

		public PaarungBuilder zwischenergebnis(Tore toreHeim, Tore toreGast) {
			return ergebnis(LAUFEND, toreHeim, toreGast);
		}

		private PaarungBuilder ergebnis(ErgebnisTyp ergebnisTyp, Tore toreHeim, Tore toreGast) {
			return ergebnisTyp(ergebnisTyp).goals(toreHeim, toreGast);
		}

		public PaarungBuilder goals(Tore toreHeim, Tore toreGast) {
			return heim(heim.withTore(toreHeim)).gast(gast.withTore(toreGast));
		}

	}

	public PaarungView viewForTeam(ViewDirection viewDirection) {
		return switch (viewDirection) {
		case HEIM -> new PaarungView(HEIM, Paarung::heim, Paarung::gast);
		case AUSWAERTS -> new PaarungView(AUSWAERTS, Paarung::gast, Paarung::heim);
		};
	}

}