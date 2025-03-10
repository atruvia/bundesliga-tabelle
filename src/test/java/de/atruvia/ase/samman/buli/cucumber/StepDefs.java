package de.atruvia.ase.samman.buli.cucumber;

import static de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder.paarung;
import static java.lang.Integer.parseInt;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.atruvia.ase.samman.buli.domain.DefaultTabelle;
import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Gegebensei;
import io.cucumber.java.de.Wenn;

public class StepDefs {

	private static final Map<String, Function<TabellenPlatz, Object>> accessors = Map.ofEntries( //
			entry("Team", t -> t.team().name()), //
			entry("Platz", TabellenPlatz::platz), //
			entry("Spiele", TabellenPlatz::spiele), //
			entry("Siege", TabellenPlatz::siege), //
			entry("Unentschieden", TabellenPlatz::unentschieden), //
			entry("Niederlagen", TabellenPlatz::niederlagen), //
			entry("Punkte", TabellenPlatz::punkte), //
			entry("Tore", TabellenPlatz::gesamtTore), //
			entry("Gegentore", TabellenPlatz::gesamtGegentore), //
			entry("Tordifferenz", TabellenPlatz::torDifferenz), //
			entry("Tendenz", t -> t.tendenz().ergebnisse().stream().map(Ergebnis::name).map(n -> n.substring(0, 1))
					.collect(joining())) //
	);

	List<Paarung> paarungen = new ArrayList<>();
	Tabelle tabelle = new DefaultTabelle();
	List<TabellenPlatz> entries;

	@Gegebensei("ein Spielplan")
	@Gegebensei("der Spielplan")
	public void der_spielplan(DataTable dataTable) {
		for (var row : dataTable.asMaps()) {
			einSpielGegenMitErgebnis(row.get("Heim"), row.get("Gast"), row.get("Ergebnis"));
		}
	}

	@Gegebensei("^ein Spiel \"([^\"]*)\" gegen \"([^\"]*)\" mit Ergebnis \"([^\"]*)\"$")
	public void einSpielGegenMitErgebnis(String teamHeim, String teamGast, String ergebnis) {
		String[] split = ergebnis.split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("Cannot split " + ergebnis + " into two parts");
		}
		paarungen.add(paarung(teamHeim, teamGast).endergebnis(parseInt(split[0]), parseInt(split[1])).build());
	}

	@Wenn("die Tabelle berechnet wird")
	public void die_tabelle_berechnet_wird() {
		entries = paarungen.stream().reduce(tabelle, Tabelle::add, (t1, t2) -> t1).entries();
	}

	@Dann("ist die Tabelle")
	public void ist_die_tabelle(DataTable dataTable) {
		var iterator = entries.iterator();
		assertSoftly(s -> {
			for (var row : dataTable.asMaps()) {
				var platz = iterator.next();
				for (var entry : row.entrySet()) {
					var name = entry.getKey();
					var value = attributeValue(platz, name);
					s.assertThat(value).describedAs("Attribute '%s' differs in row %s", name, row)
							.hasToString(entry.getValue());
				}
			}
			s.assertThat(iterator).describedAs("Expected more elements than present").toIterable().isEmpty();
		});
	}

	private static Object attributeValue(TabellenPlatz platz, String attributeName) {
		return accessor(attributeName).apply(platz);
	}

	private static Function<TabellenPlatz, Object> accessor(String attributeName) {
		return requireNonNull(accessors.get(attributeName), () -> "unknown attribute named '" + attributeName + "'");
	}

}
