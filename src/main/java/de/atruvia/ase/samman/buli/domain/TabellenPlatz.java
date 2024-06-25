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
import de.atruvia.ase.samman.buli.util.Merger.Mergeable;
import lombok.Builder;
import lombok.NonNull;
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

	@NonNull
	Team team;
	@With
	int platz;
	int spiele;
	@NonNull
	List<PaarungView> paarungen;
	int punkte;
	@NonNull
	Map<ViewDirection, Integer> tore;
	@NonNull
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
		builder.platz = platz;
		builder.team = team;
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

		public TabellenPlatzBuilder paarung(PaarungView paarung) {
			this.paarungen.add(paarung);
			return spiele(1) //
					.punkte(paarung.ergebnis().punkte()) //
					.withTore(paarung.direction(), paarung.tore()) //
					.withGegentore(paarung.direction(), paarung.gegentore()) //
					.laufendesSpiel(paarung.isLaufend() ? paarung : null);
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
				.team(mergeTeams(team, other.team)) //
				.paarungen(merge(paarungen, other.paarungen)) //
				.spiele(sum(spiele, other.spiele)) //
				.punkte(sum(punkte, other.punkte)) //
				.tore(merge(Integer::sum, tore, other.tore)) //
				.gegentore(merge(Integer::sum, gegentore, other.gegentore)) //
				.laufendesSpiel(lastNonNull(laufendesSpiel, other.laufendesSpiel)) //
				.build();
	}

	private Team mergeTeams(Team team1, Team team2) {
		return Objects.equals(team1, team2) //
				? team1 //
				: new Team(checkUnique(team1.id(), team2.id()), lastNonNull(team1.name(), team2.name()),
						lastNonNull(team1.wappen(), team2.wappen()));
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