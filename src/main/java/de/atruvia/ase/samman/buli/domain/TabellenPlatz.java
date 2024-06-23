package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.util.Merger.enforceUnique;
import static de.atruvia.ase.samman.buli.util.Merger.lastNonNull;
import static de.atruvia.ase.samman.buli.util.Merger.merge;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.generate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.ValueObject;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection;
import de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier;
import de.atruvia.ase.samman.buli.util.Merger.Mergeable;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@ValueObject
public class TabellenPlatz implements Mergeable<TabellenPlatz> {

	@Value
	public static class Tendenz {

		List<Ergebnis> ergebnisse;

		public static Tendenz fromLatestGameAtEnd(List<Ergebnis> ergebnisse, int count) {
			return new Tendenz(createReversed(ergebnisse, count));
		}

		private static <T> List<T> createReversed(List<T> source, int maxLength) {
			var it = source.listIterator(source.size());
			return generate(() -> it.hasPrevious() ? it.previous() : null) //
					.limit(maxLength) //
					.takeWhile(Objects::nonNull) //
					.toList();
		}

	}

	@Value
	static class ErgebnisEntry {
		Ergebnis ergebnis;
		ErgebnisTyp ergebnisTyp;
		ViewDirection viewDirection;
		int tore;
		TeamIdentifier identifierGegner;
		int gegenTore;
	}

	TeamIdentifier identifier;
	URI wappen;
	@With
	int platz;
	@With
	String teamName;
	int spiele;
	List<ErgebnisEntry> ergebnisse;
	int punkte;
	Map<ViewDirection, Integer> tore;
	Map<ViewDirection, Integer> gegentore;
	PaarungView laufendesSpiel;

	public List<Ergebnis> ergebnisse() {
		return collectToList(ergebnisseEntryStream());
	}

	public List<Ergebnis> ergebnisse(ErgebnisTyp... ergebnisTyp) {
		return collectToList(ergebnisseEntryStream().filter(e -> entryErgebnisIsTypeOf(e, ergebnisTyp)));
	}

	Stream<ErgebnisEntry> ergebnisseEntryStream() {
		return ergebnisse.stream();
	}

	private static List<Ergebnis> collectToList(Stream<ErgebnisEntry> filter) {
		return filter.map(ErgebnisEntry::ergebnis).toList();
	}

	private static boolean entryErgebnisIsTypeOf(ErgebnisEntry e, ErgebnisTyp... ergebnisTyp) {
		return asList(ergebnisTyp).contains(e.ergebnisTyp());
	}

	public int gesamtTore() {
		return heimtore() + auswaertsTore();
	}

	public int gesamtGegentore() {
		return heimGegentore() + auswaertsGegentore();
	}

	public int heimtore() {
		return tore().getOrDefault(HEIM, 0);
	}

	public int auswaertsTore() {
		return tore().getOrDefault(AUSWAERTS, 0);
	}

	public int heimGegentore() {
		return gegentore().getOrDefault(HEIM, 0);
	}

	public int auswaertsGegentore() {
		return gegentore().getOrDefault(AUSWAERTS, 0);
	}

	public int torDifferenz() {
		return gesamtTore() - gesamtGegentore();
	}

	public TabellenPlatzBuilder toBuilder() {
		TabellenPlatzBuilder builder = new TabellenPlatzBuilder();
		builder.identifier = identifier;
		builder.wappen = wappen;
		builder.platz = platz;
		builder.teamName = teamName;
		builder.spiele = spiele;
		builder.ergebnisse = new ArrayList<>(ergebnisse);
		builder.punkte = punkte;
		builder.tore = new HashMap<>(tore);
		builder.gegentore = new HashMap<>(gegentore);
		builder.laufendesSpiel = laufendesSpiel;
		return builder;
	}

	public static class TabellenPlatzBuilder {

		public TabellenPlatzBuilder() {
			ergebnisse = new ArrayList<>();
			tore = new HashMap<>();
			gegentore = new HashMap<>();
		}

		public TabellenPlatzBuilder team(TeamIdentifier identifier, String name, URI wappen) {
			this.identifier = identifier;
			this.teamName = name;
			this.wappen = wappen;
			return this;
		}

		public TabellenPlatzBuilder ergebnis(Ergebnis ergebnis, ErgebnisTyp ergebnisTyp, ViewDirection viewDirection,
				int tore, TeamIdentifier gegnerIdentifier, int gegenTore) {
			this.ergebnisse
					.add(new ErgebnisEntry(ergebnis, ergebnisTyp, viewDirection, tore, gegnerIdentifier, gegenTore));
			return this;
		}

		public TabellenPlatzBuilder withTore(ViewDirection viewDirection, int anzahl) {
			this.tore.put(viewDirection, anzahl);
			return this;
		}
		
		public TabellenPlatzBuilder withGegentore(ViewDirection direction, int anzahl) {
			this.gegentore.put(direction, anzahl);
			return this;
		}

	}

	@Override
	public TabellenPlatz mergeWith(TabellenPlatz other) {
		return builder() //
				.identifier(enforceUnique(identifier, other.identifier)) //
				.teamName(lastNonNull(teamName, other.teamName)) //
				.ergebnisse(merge(ergebnisse, other.ergebnisse)) //
				.spiele(merge(spiele, other.spiele)) //
				.punkte(merge(punkte, other.punkte)) //
				.tore(merge(Integer::sum, tore, other.tore)) //
				.gegentore(merge(Integer::sum, gegentore, other.gegentore)) //
				.wappen(lastNonNull(wappen, other.wappen)) //
				.laufendesSpiel(lastNonNull(laufendesSpiel, other.laufendesSpiel)) //
				.build();
	}

	public int siege() {
		return countAnzahl(SIEG);
	}

	public int unentschieden() {
		return countAnzahl(UNENTSCHIEDEN);
	}

	public int niederlagen() {
		return countAnzahl(NIEDERLAGE);
	}

	private int countAnzahl(Ergebnis type) {
		return (int) ergebnisseEntryStream().map(ErgebnisEntry::ergebnis).filter(type::equals).count();
	}

	public Tendenz tendenz() {
		return Tendenz.fromLatestGameAtEnd(ergebnisse(BEENDET), 5);
	}

}