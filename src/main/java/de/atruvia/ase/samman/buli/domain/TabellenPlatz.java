package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.util.Merger.lastNonNull;
import static de.atruvia.ase.samman.buli.util.Merger.merge;
import static java.util.Arrays.asList;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection;
import de.atruvia.ase.samman.buli.util.Merger.Mergeable;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
public class TabellenPlatz implements Mergeable<TabellenPlatz> {

	@Value
	private static class ErgebnisEntry {
		Ergebnis ergebnis;
		ErgebnisTyp ergebnisTyp;
	}

	URI wappen;
	@With
	int platz;
	@With
	String team;
	int spiele;
	List<ErgebnisEntry> ergebnisse;
	int punkte;
	Map<ViewDirection, ToreUndGegentore> toreGegentore;
	PaarungView laufendesSpiel;

	public List<Ergebnis> ergebnisse() {
		return collectToList(ergebnisseStream());
	}

	public List<Ergebnis> ergebnisse(ErgebnisTyp... ergebnisTyp) {
		return collectToList(ergebnisseStream().filter(e -> entryErgebnisIsTypeOf(e, ergebnisTyp)));
	}

	private Stream<ErgebnisEntry> ergebnisseStream() {
		return ergebnisse.stream();
	}

	private static List<Ergebnis> collectToList(Stream<ErgebnisEntry> filter) {
		return filter.map(ErgebnisEntry::ergebnis).toList();
	}

	private static boolean entryErgebnisIsTypeOf(ErgebnisEntry e, ErgebnisTyp... ergebnisTyp) {
		return asList(ergebnisTyp).contains(e.ergebnisTyp());
	}

	private ToreUndGegentore heimToreUndGegentore() {
		return toreGegentore.getOrDefault(HEIM, ToreUndGegentore.NULL);
	}

	private ToreUndGegentore auswaertsToreUndGegentore() {
		return toreGegentore.getOrDefault(AUSWAERTS, ToreUndGegentore.NULL);
	}

	public int gesamtTore() {
		return heimtore() + auswaertsTore();
	}

	public int gesamtGegentore() {
		return heimGegentore() + auswaertsGegentore();
	}

	public int heimtore() {
		return heimToreUndGegentore().tore();
	}

	public int auswaertsTore() {
		return auswaertsToreUndGegentore().tore();
	}

	public int heimGegentore() {
		return heimToreUndGegentore().gegentore();
	}

	public int auswaertsGegentore() {
		return auswaertsToreUndGegentore().gegentore();
	}

	public TabellenPlatzBuilder toBuilder() {
		TabellenPlatzBuilder builder = new TabellenPlatzBuilder();
		builder.wappen = wappen;
		builder.platz = platz;
		builder.team = team;
		builder.spiele = spiele;
		builder.ergebnisse = new ArrayList<>(ergebnisse);
		builder.punkte = punkte;
		builder.toreGegentore = new HashMap<>(toreGegentore);
		builder.laufendesSpiel = laufendesSpiel;
		return builder;
	}

	public static class TabellenPlatzBuilder {

		public TabellenPlatzBuilder() {
			ergebnisse = new ArrayList<>();
			toreGegentore = new HashMap<>();
		}

		public TabellenPlatzBuilder ergebnis(Ergebnis ergebnis, ErgebnisTyp ergebnisTyp) {
			ergebnisse.add(new ErgebnisEntry(ergebnis, ergebnisTyp));
			return this;
		}

		public TabellenPlatzBuilder toreUndGegentore(ViewDirection viewDirection, ToreUndGegentore toreUndGegentore) {
			toreGegentore.put(viewDirection, toreUndGegentore);
			return this;
		}

	}

	public int torDifferenz() {
		return gesamtTore() - gesamtGegentore();
	}

	@Override
	public TabellenPlatz mergeWith(TabellenPlatz other) {
		return builder() //
				.team(lastNonNull(team, other.team)) //
				.ergebnisse(merge(ergebnisse, other.ergebnisse)) //
				.spiele(merge(spiele, other.spiele)) //
				.punkte(merge(punkte, other.punkte)) //
				.toreGegentore(merge(toreGegentore, other.toreGegentore)) //
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
		return (int) ergebnisseStream().map(ErgebnisEntry::ergebnis).filter(type::equals).count();
	}

}