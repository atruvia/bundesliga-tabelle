package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.ValueObject;

import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz.TabellenPlatzBuilder;
import de.atruvia.ase.samman.buli.domain.Team.TeamId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@ValueObject
public class Tabelle {

	@Value
	@Accessors(fluent = true)
	private static class OrdnungsElement implements Comparable<OrdnungsElement> {

		private static final Comparator<OrdnungsElement> comparator = comparing(value(TabellenPlatz::punkte)) //
				.thenComparing(value(TabellenPlatz::torDifferenz)) //
				.thenComparing(value(TabellenPlatz::gesamtTore)) //
				.thenComparing(direkterVergleichGesamt()) //
				.thenComparing(direkterVergleichAuswaertsTore()) //
				.thenComparing(value(TabellenPlatz::auswaertsTore)) //
				.reversed();

		private static Comparator<OrdnungsElement> direkterVergleichGesamt() {
			return compareWithSwapped((that, other) -> whereToreIs(that.tabellenPlatz, gegnerIs(identifier(other))));
		}

		private static Comparator<OrdnungsElement> direkterVergleichAuswaertsTore() {
			return compareWithSwapped(
					(that, other) -> whereToreIs(that.tabellenPlatz, gegnerIs(identifier(other)).and(isAuswaerts())));
		}

		private static <T, R extends Comparable<R>> Comparator<T> compareWithSwapped(BiFunction<T, T, R> biFunction) {
			return (o1, o2) -> biFunction.apply(o1, o2).compareTo(biFunction.apply(o2, o1));
		}

		private static TeamId identifier(OrdnungsElement ordnungsElement) {
			return ordnungsElement.tabellenPlatz.identifier();
		}

		private static Predicate<PaarungView> gegnerIs(TeamId gegner) {
			return e -> Objects.equals(e.gegner().team().id(), gegner);
		}

		private static Predicate<PaarungView> isAuswaerts() {
			return e -> Objects.equals(e.direction(), AUSWAERTS);
		}

		private static int whereToreIs(TabellenPlatz tabellenPlatz, Predicate<PaarungView> filter) {
			return tabellenPlatz.paarungenStream().filter(filter).map(PaarungView::tore).mapToInt(Integer::valueOf)
					.sum();
		}

		@Getter(value = PRIVATE)
		TabellenPlatz tabellenPlatz;

		@Override
		public int hashCode() {
			return 0;
		}

		private static <T> Function<OrdnungsElement, T> value(Function<TabellenPlatz, T> function) {
			return t -> function.apply(t.tabellenPlatz);
		}

		@Override
		public boolean equals(Object o) {
			return comparator.compare(this, (OrdnungsElement) o) == 0;
		}

		@Override
		public int compareTo(OrdnungsElement other) {
			return comparator.thenComparing(comparing(e -> e.tabellenPlatz().teamName())).compare(this, other);
		}

	}

	private final Map<TeamId, TabellenPlatz> entries;

	public Tabelle() {
		this(emptyMap());
	}

	public Tabelle add(Paarung paarung) {
		Map<TeamId, TabellenPlatz> entries = new HashMap<>(this.entries);
		entries = addInternal(entries, paarung.viewForTeam(HEIM));
		entries = addInternal(entries, paarung.viewForTeam(AUSWAERTS));
		return new Tabelle(Map.copyOf(entries));
	}

	private static Map<TeamId, TabellenPlatz> addInternal(Map<TeamId, TabellenPlatz> entries, PaarungView paarung) {
		entries.merge(paarung.self().team().id(), newEntry(paarung), TabellenPlatz::mergeWith);
		return entries;
	}

	private static TabellenPlatz newEntry(PaarungView paarung) {
		var team = paarung.self().team();
		TabellenPlatzBuilder builder = TabellenPlatz.builder() //
				.team(team.id(), team.name(), team.wappen());
		if (!paarung.isGeplant()) {
			builder = builder.spiele(1) //
					.paarung(paarung) //
					.punkte(paarung.ergebnis().punkte()) //
					.withTore(paarung.direction(), paarung.tore()) //
					.withGegentore(paarung.direction(), paarung.gegentore()) //
					.laufendesSpiel(paarung.isLaufend() ? paarung : null) //
			;
		}

		return builder.build();
	}

	public List<TabellenPlatz> getEntries() {
		// TODO make it side-affect-free, does it work W/O zip!?
		AtomicInteger platz = new AtomicInteger(1);
		Map<OrdnungsElement, List<TabellenPlatz>> platzGruppen = entries.values().stream()
				.collect(groupingBy(OrdnungsElement::new));
		return platzGruppen.entrySet().stream() //
				.sorted(Entry.comparingByKey()) //
				.map(Entry::getValue) //
				.flatMap(t -> makeGroup(platz, t)) //
				.toList();
	}

	private static Stream<TabellenPlatz> makeGroup(AtomicInteger platz, List<TabellenPlatz> tabellenPlaetze) {
		int no = platz.getAndAdd(tabellenPlaetze.size());
		return tabellenPlaetze.stream().sorted(comparing(OrdnungsElement::new)).map(tp -> tp.withPlatz(no));
	}

}
