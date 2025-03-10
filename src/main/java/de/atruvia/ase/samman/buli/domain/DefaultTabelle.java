package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.ValueObject;

import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@ValueObject
public class DefaultTabelle implements Tabelle {

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
			return compareWithSwapped((that, other) -> whereToreIs(that.tabellenPlatz, gegnerIs(other.team())));
		}

		private static Comparator<OrdnungsElement> direkterVergleichAuswaertsTore() {
			return compareWithSwapped(
					(that, other) -> whereToreIs(that.tabellenPlatz, gegnerIs(other.team()).and(isAuswaerts())));
		}

		private static <T, R extends Comparable<R>> Comparator<T> compareWithSwapped(BiFunction<T, T, R> biFunction) {
			return (o1, o2) -> biFunction.apply(o1, o2).compareTo(biFunction.apply(o2, o1));
		}

		private Team team() {
			return tabellenPlatz.team();
		}

		private static Predicate<PaarungView> gegnerIs(Team gegner) {
			return e -> Objects.equals(e.gegner().team().id(), gegner.id());
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
			return comparator.thenComparing(e -> e.team().name()).compare(this, other);
		}

	}

	private final Collection<TabellenPlatz> entries;

	public DefaultTabelle() {
		this(emptyList());
	}

	@Override
	public Tabelle add(Paarung paarung) {
		var existing = entries().stream();
		var toAdd = Stream.of(newEntry(paarung.viewForTeam(HEIM)), newEntry(paarung.viewForTeam(AUSWAERTS)));
		var newEntries = concat(existing, toAdd) //
				.collect(toMap(t -> t.team().id(), identity(), TabellenPlatz::mergeWith)) //
				.values();
		return new DefaultTabelle(newEntries);
	}

	private static TabellenPlatz newEntry(PaarungView paarung) {
		var builder = TabellenPlatz.builder().team(paarung.self().team());
		return (paarung.isGeplant() ? builder : builder.paarung(paarung)).build();
	}

	public List<TabellenPlatz> entries() {
		// TODO make it side-effect-free
		AtomicInteger counter = new AtomicInteger(1);
		Map<OrdnungsElement, List<TabellenPlatz>> platzGruppen = entries.stream()
				.collect(groupingBy(OrdnungsElement::new));
		return new TreeMap<>(platzGruppen).values().stream() //
				.flatMap(t -> makeGroup(counter, t)) //
				.toList();
	}

	private static Stream<TabellenPlatz> makeGroup(AtomicInteger counter, List<TabellenPlatz> tabellenPlaetze) {
		Rank rank = new Rank(counter.getAndAdd(tabellenPlaetze.size()));
		return tabellenPlaetze.stream() //
				.sorted(comparing(OrdnungsElement::new)) //
				.map(tp -> tp.withRank(rank));
	}

}
