package de.atruvia.ase.samman.buli.domain;

import static com.google.common.collect.Streams.concat;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.PaarungMother.createPaarungen;
import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowingConsumer;
import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry.EntryBuilder;
import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz.Tendenz;

class TabelleTest {

	Paarung[] paarungen;
	Tabelle sut = new Tabelle();

	@Test
	void zweiMannschaftenKeinSpiel() {
		gegebenSeienDiePaarungen(paarung("Team 1", "Team 2"), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team 1  |     0|    0|            0|          0|     0|         0|              0|           0
						1    |Team 2  |     0|    0|            0|          0|     0|         0|              0|           0""");
	}

	@Test
	void zweiMannschaftenEinSpielKeineTore() {
		gegebenSeienDiePaarungen(paarung("Team 1", "Team 2").endergebnis(0, 0), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team 1  |     1|    0|            1|          0|     1|         0|              0|           0
						1    |Team 2  |     1|    0|            1|          0|     1|         0|              0|           0""");
	}

	@Test
	void mannschaftMitMehrPunktenIstWeiterOben() {
		gegebenSeienDiePaarungen(paarung("Team 1", "Team 2").endergebnis(0, 1), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team 2  |     1|    1|            0|          0|     3|         1|              0|           1
						2    |Team 1  |     1|    0|            0|          1|     0|         0|              1|          -1""");
	}

	@Test
	void zweiMannschaftenZweiSpieleMitToren() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team 1  |     2|    1|            0|          1|     3|         1|              1|           0
						1    |Team 2  |     2|    1|            0|          1|     3|         1|              1|           0""");
	}

	@Test
	void dieFolgendeMannschaftIstPlatzDrei___antiPattern_toMuchVerificationsViaBDDStyle() {
		// Dieser Test, testet viel zu viel, denn er soll eigentlich nur verifizieren,
		// ob die Platznummerierung (1,1,3) stimmt
		// Diesen Test gibt es auch als Cucumber Test
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").endergebnis(1, 0), //
				paarung("Team 1", "Team 3").endergebnis(1, 0), //
				paarung("Team 2", "Team 3").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team 1  |     3|    2|            0|          1|     6|         2|              1|           1
						1    |Team 2  |     3|    2|            0|          1|     6|         2|              1|           1
						3    |Team 3  |     2|    0|            0|          2|     0|         0|              2|          -2""");
	}

	@Test
	void dieFolgendeMannschaftIstPlatzDrei() {
		// Diesen Test gibt es auch als Cucumber Test (und dieser lässt sich besser
		// lesen)
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").endergebnis(1, 0), //
				paarung("Team 1", "Team 3").endergebnis(1, 0), //
				paarung("Team 2", "Team 3").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertThat(e1.teamName()).isEqualTo("Team 1");
					assertThat(e1.platz()).isEqualTo(1);
				}, //
				e2 -> {
					assertThat(e2.teamName()).isEqualTo("Team 2");
					assertThat(e2.platz()).isEqualTo(1);
				}, //
				e3 -> {
					assertThat(e3.teamName()).isEqualTo("Team 3");
					assertThat(e3.platz()).isEqualTo(3);
				} //

		);
	}

	@Test
	void team2IstImDirektenVergleichBesserAlsTeam1() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(0, 1), //
				paarung("Team GegnerXvon1", "Team 1").endergebnis(0, 1), //
				paarung("Team 2", "Team GegnerXvon2").endergebnis(0, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|teamName        |spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team GegnerXvon2|1     |1    |0            |0          |3     |1         |0              |1
						2    |Team 2          |2     |1    |0            |1          |3     |1         |1              |0
						3    |Team 1          |2     |1    |0            |1          |3     |1         |1              |0
						4    |Team GegnerXvon1|1     |0    |0            |1          |0     |0         |1              |-1""");
	}

	@Test
	void anzahlAuswaertsToreImDirektenVergleichZiehtVorDenGesamtAuswaertsToren() {
		gegebenSeienDiePaarungen( //
				paarung("Team 2", "Team 1").endergebnis(0, 1), //
				paarung("Team 1", "Team 2").endergebnis(1, 2), // <-- Team 2 hat 2, Team 1 hat 1 Auswärtstor
				paarung("Team GegnerXvon1", "Team 1").endergebnis(0, 9), //
				paarung("Team 1", "Team GegnerXvon1").endergebnis(9, 0), //
				paarung("Team GegnerXvon2", "Team 2").endergebnis(0, 8), //
				paarung("Team 2", "Team GegnerXvon2").endergebnis(10, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.teamName()).isEqualTo("Team 2"), //
				e2 -> assertThat(e2.teamName()).isEqualTo("Team 1"), //
				e3 -> dontCare(), //
				e4 -> dontCare() //
		);
	}

	@Test
	void tordifferenzToreDirekterVergleichIDentischDannZaehlenMehrAuswaertsTore() {
		gegebenSeienDiePaarungen( //
				paarung("Team 2", "Team 1").endergebnis(0, 1), //
				paarung("Team 1", "Team 2").endergebnis(0, 1), //
				paarung("Team 1", "Team GegnerXvon1").endergebnis(3, 0), //
				paarung("Team GegnerXvon1", "Team 1").endergebnis(3, 0), //
				paarung("Team 2", "Team GegnerXvon2").endergebnis(2, 1), //
				paarung("Team GegnerXvon2", "Team 2").endergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.teamName()).isEqualTo("Team 2"), //
				e2 -> assertThat(e2.teamName()).isEqualTo("Team 1"), //
				e3 -> dontCare(), //
				e4 -> dontCare() //
		);
	}

	private static void dontCare() {
	}

	@Test
	void punktUndTorGleichAberMehrAuswärtsTore() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 2), //
				paarung("Team 2", "Team 1").endergebnis(0, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.teamName()).isEqualTo("Team 2"), //
				e2 -> assertThat(e2.teamName()).isEqualTo("Team 1") //
		);
	}

	@Test
	void wappenIstImmerDasDerLetztenPaarung() {
		gegebenSeienDiePaarungen(
				paarung("Team 1", "Team 2", create("proto://wappenAlt1"), create("proto://wappenAlt2")),
				paarung("Team 2", "Team 1", create("proto://wappenNeu2"), create("proto://wappenNeu1")));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.wappen()).isEqualTo(create("proto://wappenNeu1")), //
				e2 -> assertThat(e2.wappen()).isEqualTo(create("proto://wappenNeu2")) //
		);
	}

	@Test
	void nullWappenWerdenNichtUebernommen() {
		gegebenSeienDiePaarungen(
				paarung("Team 1", "Team 2", create("proto://wappenAlt1"), create("proto://wappenAlt2")),
				paarung("Team 2", "Team 1", create("proto://wappenNeu2"), null));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.wappen()).isEqualTo(create("proto://wappenAlt1")), //
				e2 -> assertThat(e2.wappen()).isEqualTo(create("proto://wappenNeu2")) //
		);
	}

	@Test
	void wennEinWappenInAllenPaarungenNullIstIstEsNull() {
		gegebenSeienDiePaarungen( //
				paarung("Team mit Wappen", "Team ohne Wappen", create("proto://wappen1"), null), //
				paarung("Team ohne Wappen", "Team mit Wappen", null, create("proto://wappen1")) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.wappen()).isEqualTo(create("proto://wappen1")), //
				e2 -> assertThat(e2.wappen()).isNull() //
		);
	}

	@Test
	void beiAenderndemMannschaftsnamenWirdDerLetzteUebernommen() {
		String team1 = "Team 1";
		String team2 = "Team 2";
		var heimAlt = team(team1, team1);
		var gastAlt = team(team2 + "-A", team2);

		var heimNeu = team(team1, team1);
		var gastNeu = team(team2 + "-B", team2);
		gegebenSeienDiePaarungen(paarung(heimAlt, gastAlt), paarung(heimNeu, gastNeu));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.teamName()).isEqualTo(team1), //
				e2 -> assertThat(e2.teamName()).isEqualTo(team2 + "-B") //
		);
	}

	@Test
	void beiAenderndemMannschaftsnamenNullWirdNichtUebernommen() {
		String team1 = "Team 1";
		String team2 = "Team 2";
		var heimAlt = team(team1, team1);
		var gastAlt = team(team2, team2);

		var heimNeu = team(team1, team1);
		var gastNeu = team(null, team2);
		gegebenSeienDiePaarungen( //
				paarung(heimAlt, gastAlt), paarung(heimNeu, gastNeu));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.teamName()).isEqualTo(team1), //
				e2 -> assertThat(e2.teamName()).isEqualTo(team2) //
		);
	}

	@Test
	void keineSpieleKeineErgebnisse() {
		gegebenSeienDiePaarungen(paarung("Team 1", "Team 2"), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.ergebnisse()).isEmpty(), //
				e2 -> assertThat(e2.ergebnisse()).isEmpty() //
		);
	}

	@Test
	void zweiSpieleErgebnisse_dieLetztePaarungIstVorneInDerListe() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").endergebnis(1, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.ergebnisse()).containsExactly(SIEG, UNENTSCHIEDEN), //
				e2 -> assertThat(e2.ergebnisse()).containsExactly(NIEDERLAGE, UNENTSCHIEDEN) //
		);
	}

	@Test
	void laufendeSpieleWerdenAusgewiesen() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").zwischenergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertThat(e1.teamName()).isEqualTo("Team 1");
					assertThat(e1.laufendesSpiel().tore()).isEqualTo(1);
					assertThat(e1.laufendesSpiel().gegentore()).isEqualTo(2);
				}, //
				e2 -> {
					assertThat(e2.teamName()).isEqualTo("Team 2");
					assertThat(e2.laufendesSpiel().tore()).isEqualTo(2);
					assertThat(e2.laufendesSpiel().gegentore()).isEqualTo(1);
				} //

		);
	}

	@Test
	void tendenz_letzterSpieltagGanzVorneRestNull() {
		gegebenSeienDiePaarungen(paarung("Team 1", "Team 2").endergebnis(1, 0));
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz("Team 1", SIEG);
	}

	@Test
	void tendenz_beinhaltetKeineLaufendenSpiele() {
		gegebenSeienDiePaarungen( //
				paarung("Team 1", "Team 2").endergebnis(1, 0), //
				paarung("Team 2", "Team 1").zwischenergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz("Team 1", SIEG);
	}

	@Test
	void tendenz_letzterSpieltagGanzVorneMaximalFuenfElemente() {
		var team = "Team 1";
		gegebenSeienDiePaarungen(
				createPaarungen(team, SIEG, SIEG, NIEDERLAGE, NIEDERLAGE, UNENTSCHIEDEN, UNENTSCHIEDEN));
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz(team, UNENTSCHIEDEN, UNENTSCHIEDEN, NIEDERLAGE, NIEDERLAGE, SIEG);
	}

	private static PaarungBuilder paarung(String teamHeim, String teamGast) {
		return paarung(team(teamHeim, teamHeim), team(teamGast, teamGast));
	}

	private static PaarungBuilder paarung(String teamHeim, String teamGast, URI wappenHeim, URI wappenGast) {
		return Paarung.builder()
				.heim(Entry.builder().team(Team.builder().name(teamHeim).wappen(wappenHeim).build()).build())
				.gast(Entry.builder().team(Team.builder().name(teamGast).wappen(wappenGast).build()).build());
	}

	private static EntryBuilder team(String team, String identifier) {
		return Entry.builder().team(Team.builder().identifier(teamIdentifier(identifier)).name(team).build());
	}

	private static PaarungBuilder paarung(EntryBuilder heim, EntryBuilder gast) {
		return Paarung.builder().heim(heim.build()).gast(gast.build());
	}

	private void gegebenSeienDiePaarungen(PaarungBuilder... paarungen) {
		this.paarungen = stream(paarungen).map(PaarungBuilder::build).toArray(Paarung[]::new);
	}

	private void gegebenSeienDiePaarungen(Collection<Paarung> paarungen) {
		this.paarungen = paarungen.toArray(Paarung[]::new);
	}

	private void wennDieTabelleBerechnetWird() {
		stream(this.paarungen).forEach(sut::add);
	}

	private void dannIstDieTabelle(String expected) {
		assertThat(print(sut.getEntries())).isEqualTo(line(stream(expected.split("\\|")).map(String::trim)));
	}

	@SafeVarargs
	private void dannIstDieTabelle(ThrowingConsumer<? super TabellenPlatz>... requirements) {
		assertThat(sut.getEntries()).satisfiesExactly(requirements);
	}

	private void dannIstDieTendenz(String team, Ergebnis... tendenz) {
		assertThat(tendenzForTeam(team).ergebnisse()).containsExactly(tendenz);
	}

	private Tendenz tendenzForTeam(String team) {
		var tabellenPlatz = sut.getEntries().stream().filter(t -> t.teamName().equals(team)).findFirst()
				.orElseThrow(() -> new IllegalStateException("No entry for team " + team));
		return tabellenPlatz.tendenz();
	}

	private static String print(List<TabellenPlatz> plaetze) {
		List<String> attribs = asList("platz", "teamName", "spiele", "siege", "unentschieden", "niederlagen", "punkte",
				"gesamtTore", "gesamtGegentore", "torDifferenz");
		Stream<String> header = Stream.of(line(attribs.stream()));
		Stream<String> values = plaetze.stream().map(t -> print(t, attribs));
		return concat(header, values).collect(joining("\n"));
	}

	private static String print(TabellenPlatz platz, List<String> attribs) {
		return line(values(attribs, platz).stream());
	}

	private static String line(Stream<?> objects) {
		return objects.map(Object::toString).collect(joining("|"));
	}

	private static List<Object> values(List<String> attribs, TabellenPlatz platz) {
		List<Method> declaredMethods = asList(platz.getClass().getDeclaredMethods());
		return attribs.stream().map(a -> readValue(platz, declaredMethods, a)).toList();
	}

	private static Object readValue(Object bean, List<Method> declaredMethods, String attribName) {
		Method readMethod = declaredMethods.stream().filter(p -> p.getName().equals(attribName)).findFirst()
				.orElseThrow(() -> new IllegalStateException("no attribute with name " + attribName));
		try {
			return readMethod.invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
