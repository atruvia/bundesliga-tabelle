package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.util.Merger.checkUnique;
import static de.atruvia.ase.samman.buli.util.Merger.lastNonNull;
import static de.atruvia.ase.samman.buli.util.Merger.merge;
import static de.atruvia.ase.samman.buli.util.Merger.sum;
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
import lombok.Singular;
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

	TeamIdentifier identifier;
	URI wappen;
	@With
	int platz;
	@With
	String teamName;
	int spiele;
	@Singular(value = "paarung")
	List<PaarungView> paarungen;
	int punkte;
	Map<ViewDirection, Integer> tore;
	Map<ViewDirection, Integer> gegentore;
	PaarungView laufendesSpiel;

	public List<Ergebnis> ergebnisse() {
		return collectToList(paarungenStream());
	}

	public List<Ergebnis> ergebnisse(ErgebnisTyp... ergebnisTyp) {
		return collectToList(paarungenStream().filter(e -> entryErgebnisIsTypeOf(e, ergebnisTyp)));
	}

	Stream<PaarungView> paarungenStream() {
		return paarungen.stream();
	}

	private static List<Ergebnis> collectToList(Stream<PaarungView> stream) {
		return stream.map(PaarungView::ergebnis).toList();
	}

	private static boolean entryErgebnisIsTypeOf(PaarungView paarung, ErgebnisTyp... ergebnisTyp) {
		return asList(ergebnisTyp).contains(paarung.ergebnisTyp());
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
		builder.paarungen = new ArrayList<>(paarungen);
		builder.punkte = punkte;
		builder.tore = new HashMap<>(tore);
		builder.gegentore = new HashMap<>(gegentore);
		builder.laufendesSpiel = laufendesSpiel;
		return builder;
	}

	public static class TabellenPlatzBuilder {

		public TabellenPlatzBuilder() {
			paarungen = new ArrayList<>();
			tore = new HashMap<>();
			gegentore = new HashMap<>();
		}

		public TabellenPlatzBuilder team(TeamIdentifier identifier, String name, URI wappen) {
			this.identifier = identifier;
			this.teamName = name;
			this.wappen = wappen;
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
				.identifier(checkUnique(identifier, other.identifier)) //
				.teamName(lastNonNull(teamName, other.teamName)) //
				.paarungen(merge(paarungen, other.paarungen)) //
				.spiele(sum(spiele, other.spiele)) //
				.punkte(sum(punkte, other.punkte)) //
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
		return (int) paarungenStream().map(PaarungView::ergebnis).filter(type::equals).count();
	}

	public Tendenz tendenz() {
		return Tendenz.fromLatestGameAtEnd(ergebnisse(BEENDET), 5);
	}

}