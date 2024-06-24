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
		Function<Paarung, Entry> self;
		Function<Paarung, Entry> gegner;
		@Getter(lazy = true)
		Ergebnis ergebnis = calcErgebnis();

		public Entry self() {
			return self.apply(paarung());
		}

		public Entry gegner() {
			return gegner.apply(paarung());
		}

		public int tore() {
			return self().tore();
		}

		public int gegentore() {
			return gegner().tore();
		}

		public boolean isGeplant() {
			return paarung().isGeplant();
		}

		public boolean isLaufend() {
			return paarung().isLaufend();
		}

		private Ergebnis calcErgebnis() {
			var tore = tore();
			var gegentore = gegentore();
			if (tore == gegentore) {
				return UNENTSCHIEDEN;
			} else if (tore > gegentore) {
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
	@Builder(toBuilder = true)
	public static class Entry {
		Team team;
		@With
		int tore;

		public Entry(Team team, int tore) {
			if (tore < 0) {
				throw new IllegalArgumentException("Tore darf nicht negativ sein, ist aber " + tore);
			}
			this.team = team;
			this.tore = tore;
		}

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

	public Paarung withErgebnis(int toreHeim, int toreGast) {
		return toBuilder().endergebnis(toreHeim, toreGast).build();
	}

	public ViewDirection viewDirection() {
		return HEIM;
	}

	public static class PaarungBuilder {

		public static PaarungBuilder paarung(String heim, String gast) {
			return paarung(team(heim), team(gast));
		}

		private static Team team(String name) {
			return Team.builder().name(name).build();
		}

		public static PaarungBuilder paarung(Team heim, Team gast) {
			return Paarung.builder().heim(entry(heim)).gast(entry(gast));
		}

		public PaarungBuilder endergebnis(int toreHeim, int toreGast) {
			return ergebnis(BEENDET, toreHeim, toreGast);
		}

		public PaarungBuilder zwischenergebnis(int toreHeim, int toreGast) {
			return ergebnis(LAUFEND, toreHeim, toreGast);
		}

		PaarungBuilder ergebnis(ErgebnisTyp ergebnisTyp, int toreHeim, int toreGast) {
			return ergebnisTyp(ergebnisTyp).goals(toreHeim, toreGast);
		}

		private static Entry entry(Team team) {
			return Entry.builder().team(team).build();
		}

		public PaarungBuilder goals(int toreHeim, int toreGast) {
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