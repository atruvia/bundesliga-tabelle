package de.atruvia.ase.samman.buli.domain;

import static com.google.common.collect.Streams.concat;
import static de.atruvia.ase.samman.buli.domain.Paarung.Entry.entry;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.PaarungMother.createPaarungen;
import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import de.atruvia.ase.samman.buli.domain.Team.TeamId;

class DefaultTabelleTest {

	Paarung[] paarungen;
	Tabelle sut = new DefaultTabelle();

	@Test
	void zweiMannschaftenKeinSpiel() {
		gegebenSeienDiePaarungen(paarung("Team-A", "Team-B"), paarung("Team-B", "Team-A"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein|kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team-A|A       |     0|    0|            0|          0|     0|         0|              0|           0
						1    |Team-B|B       |     0|    0|            0|          0|     0|         0|              0|           0""");
	}

	@Test
	void zweiMannschaftenEinSpielKeineTore() {
		gegebenSeienDiePaarungen(paarung("Team-A", "Team-B").endergebnis(0, 0), paarung("Team-B", "Team-A"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein|kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team-A|A       |     1|    0|            1|          0|     1|         0|              0|           0
						1    |Team-B|B       |     1|    0|            1|          0|     1|         0|              0|           0""");
	}

	@Test
	void mannschaftMitMehrPunktenIstWeiterOben() {
		gegebenSeienDiePaarungen(paarung("Team-A", "Team-B").endergebnis(0, 1), paarung("Team-B", "Team-A"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein|kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team-B|B       |     1|    1|            0|          0|     3|         1|              0|           1
						2    |Team-A|A       |     1|    0|            0|          1|     0|         0|              1|          -1""");
	}

	@Test
	void zweiMannschaftenZweiSpieleMitToren() {
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein|kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team-A|A       |     2|    1|            0|          1|     3|         1|              1|           0
						1    |Team-B|B       |     2|    1|            0|          1|     3|         1|              1|           0""");
	}

	@Test
	void dieFolgendeMannschaftIstPlatzDrei___antiPattern_toMuchVerificationsViaBDDStyle() {
		// Dieser Test, testet viel zu viel, denn er soll eigentlich nur verifizieren,
		// ob die Platznummerierung (1,1,3) stimmt
		// Diesen Test gibt es auch als Cucumber Test
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").endergebnis(1, 0), //
				paarung("Team-A", "Team-C").endergebnis(1, 0), //
				paarung("Team-B", "Team-C").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein|kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team-A|A       |     3|    2|            0|          1|     6|         2|              1|           1
						1    |Team-B|B       |     3|    2|            0|          1|     6|         2|              1|           1
						3    |Team-C|C       |     2|    0|            0|          2|     0|         0|              2|          -2""");
	}

	@Test
	void dieFolgendeMannschaftIstPlatzDrei() {
		// Diesen Test gibt es auch als Cucumber Test (und dieser lässt sich besser
		// lesen)
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").endergebnis(1, 0), //
				paarung("Team-A", "Team-C").endergebnis(1, 0), //
				paarung("Team-B", "Team-C").endergebnis(1, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertThat(e1.team().name()).isEqualTo("Team-A");
					assertThat(e1.platz()).isEqualTo(1);
				}, //
				e2 -> {
					assertThat(e2.team().name()).isEqualTo("Team-B");
					assertThat(e2.platz()).isEqualTo(1);
				}, //
				e3 -> {
					assertThat(e3.team().name()).isEqualTo("Team-C");
					assertThat(e3.platz()).isEqualTo(3);
				} //

		);
	}

	@Test
	void team2IstImDirektenVergleichBesserAlsTeam1() {
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(0, 1), //
				paarung("Team GegnerXvonA", "Team-A").endergebnis(0, 1), //
				paarung("Team-B", "Team GegnerXvonB").endergebnis(0, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(
				"""
						platz|verein          |kurzname|spiele|siege|unentschieden|niederlagen|punkte|gesamtTore|gesamtGegentore|torDifferenz
						1    |Team GegnerXvonB|B       |1     |1    |0            |0          |3     |1         |0              |1
						2    |Team-B          |B       |2     |1    |0            |1          |3     |1         |1              |0
						3    |Team-A          |A       |2     |1    |0            |1          |3     |1         |1              |0
						4    |Team GegnerXvonA|A       |1     |0    |0            |1          |0     |0         |1              |-1""");
	}

	@Test
	void anzahlAuswaertsToreImDirektenVergleichZiehtVorDenGesamtAuswaertsToren() {
		gegebenSeienDiePaarungen( //
				paarung("Team-B", "Team-A").endergebnis(0, 1), //
				paarung("Team-A", "Team-B").endergebnis(1, 2), // <-- Team-B hat 2, Team-A hat 1 Auswärtstor
				paarung("Team GegnerXvonA", "Team-A").endergebnis(0, 9), //
				paarung("Team-A", "Team GegnerXvonA").endergebnis(9, 0), //
				paarung("Team GegnerXvonB", "Team-B").endergebnis(0, 8), //
				paarung("Team-B", "Team GegnerXvonB").endergebnis(10, 0) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.team().name()).isEqualTo("Team-B"), //
				e2 -> assertThat(e2.team().name()).isEqualTo("Team-A"), //
				e3 -> dontCare(), //
				e4 -> dontCare() //
		);
	}

	@Test
	void tordifferenzToreDirekterVergleichIDentischDannZaehlenMehrAuswaertsTore() {
		gegebenSeienDiePaarungen( //
				paarung("Team-B", "Team-A").endergebnis(0, 1), //
				paarung("Team-A", "Team-B").endergebnis(0, 1), //
				paarung("Team-A", "Team GegnerXvonA").endergebnis(3, 0), //
				paarung("Team GegnerXvonA", "Team-A").endergebnis(3, 0), //
				paarung("Team-B", "Team GegnerXvonB").endergebnis(2, 1), //
				paarung("Team GegnerXvonB", "Team-B").endergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.team().name()).isEqualTo("Team-B"), //
				e2 -> assertThat(e2.team().name()).isEqualTo("Team-A"), //
				e3 -> dontCare(), //
				e4 -> dontCare() //
		);
	}

	private static void dontCare() {
	}

	@Test
	void punktUndTorGleichAberMehrAuswärtsTore() {
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 2), //
				paarung("Team-B", "Team-A").endergebnis(0, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.team().name()).isEqualTo("Team-B"), //
				e2 -> assertThat(e2.team().name()).isEqualTo("Team-A") //
		);
	}

	@Test
	void wappenIstImmerDasDerLetztenPaarung() {
		gegebenSeienDiePaarungen(
				paarung("Team-A", "Team-B", create("proto://wappenAlt1"), create("proto://wappenAlt2")),
				paarung("Team-B", "Team-A", create("proto://wappenNeu2"), create("proto://wappenNeu1")));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.team().wappen()).isEqualTo(create("proto://wappenNeu1")), //
				e2 -> assertThat(e2.team().wappen()).isEqualTo(create("proto://wappenNeu2")) //
		);
	}

	@Test
	void nullWappenWerdenNichtUebernommen() {
		gegebenSeienDiePaarungen(
				paarung("Team-A", "Team-B", create("proto://wappenAlt1"), create("proto://wappenAlt2")),
				paarung("Team-B", "Team-A", create("proto://wappenNeu2"), null));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.team().wappen()).isEqualTo(create("proto://wappenAlt1")), //
				e2 -> assertThat(e2.team().wappen()).isEqualTo(create("proto://wappenNeu2")) //
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
				e1 -> assertThat(e1.team().wappen()).isEqualTo(create("proto://wappen1")), //
				e2 -> assertThat(e2.team().wappen()).isNull() //
		);
	}

	@Test
	void beiAenderndemMannschaftsnamenWirdDerLetzteUebernommen() {
		String team1 = "Team-A";
		String team2 = "Team-B";
		var heimAlt = team(team1, teamId(team1));
		var gastAlt = team(team2 + "-X", teamId(team2));

		var heimNeu = team(team1, teamId(team1));
		var gastNeu = team(team2 + "-Y", teamId(team2));
		gegebenSeienDiePaarungen(paarung(heimAlt, gastAlt), paarung(heimNeu, gastNeu));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertSoftly(s -> {
						s.assertThat(e1.team().name()).isEqualTo(team1);
						s.assertThat(e1.team().kurzname()).isEqualTo("A");
					});
				}, //
				e2 -> {
					assertSoftly(s -> {
						s.assertThat(e2.team().kurzname()).isEqualTo("Y"); //
						s.assertThat(e2.team().name()).isEqualTo(team2 + "-Y"); //
					});
				});
	}

	@Test
	void beiAenderndemMannschaftsnamenNullWirdNichtUebernommen() {
		String team1 = "Team-A";
		String team2 = "Team-B";
		var heimAlt = team(team1, teamId(team1));
		var gastAlt = team(team2, teamId(team2));

		var heimNeu = team(team1, teamId(team1));
		var gastNeu = team(null, teamId(team2));
		gegebenSeienDiePaarungen( //
				paarung(heimAlt, gastAlt), paarung(heimNeu, gastNeu));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertSoftly(s -> {
						s.assertThat(e1.team().name()).isEqualTo(team1);
						s.assertThat(e1.team().kurzname()).isEqualTo("A");
					});
				}, //
				e2 -> {
					assertSoftly(s -> {
						s.assertThat(e2.team().name()).isEqualTo(team2); //
						s.assertThat(e2.team().kurzname()).isEqualTo("B"); //
					});
				});
	}

	@Test
	void keineSpieleKeineErgebnisse() {
		gegebenSeienDiePaarungen(paarung("Team-A", "Team-B"), paarung("Team-B", "Team-A"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> assertThat(e1.ergebnisse()).isEmpty(), //
				e2 -> assertThat(e2.ergebnisse()).isEmpty() //
		);
	}

	@Test
	void zweiSpieleErgebnisse_dieLetztePaarungIstVorneInDerListe() {
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").endergebnis(1, 1) //
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
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").zwischenergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				e1 -> {
					assertThat(e1.team().name()).isEqualTo("Team-A");
					assertThat(e1.laufendesSpiel().tore()).isEqualTo(1);
					assertThat(e1.laufendesSpiel().gegentore()).isEqualTo(2);
				}, //
				e2 -> {
					assertThat(e2.team().name()).isEqualTo("Team-B");
					assertThat(e2.laufendesSpiel().tore()).isEqualTo(2);
					assertThat(e2.laufendesSpiel().gegentore()).isEqualTo(1);
				} //

		);
	}

	@Test
	void spieltEineMannschaftZeitgleichGegenZweiAndereMannschaftenIstDiesEinFehler() {
		gegebenSeienDiePaarungen( //
				paarung("Team X", "Gegner 1").zwischenergebnis(1, 0), //
				paarung("Gegner 2", "Team X").zwischenergebnis(2, 1) //
		);
		assertThat(assertThrows(RuntimeException.class, this::wennDieTabelleBerechnetWird))
				.hasMessageContainingAll("several matches", "Gegner 1", "Gegner 2");
	}

	@Test
	void tendenz_letzterSpieltagGanzVorneRestNull() {
		gegebenSeienDiePaarungen(paarung("Team-A", "Team-B").endergebnis(1, 0));
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz("Team-A", SIEG);
	}

	@Test
	void tendenz_beinhaltetKeineLaufendenSpiele() {
		gegebenSeienDiePaarungen( //
				paarung("Team-A", "Team-B").endergebnis(1, 0), //
				paarung("Team-B", "Team-A").zwischenergebnis(2, 1) //
		);
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz("Team-A", SIEG);
	}

	@Test
	void tendenz_letzterSpieltagGanzVorneMaximalFuenfElemente() {
		var team = "Team-A";
		gegebenSeienDiePaarungen(
				createPaarungen(team, SIEG, SIEG, NIEDERLAGE, NIEDERLAGE, UNENTSCHIEDEN, UNENTSCHIEDEN));
		wennDieTabelleBerechnetWird();
		dannIstDieTendenz(team, UNENTSCHIEDEN, UNENTSCHIEDEN, NIEDERLAGE, NIEDERLAGE, SIEG);
	}

	private static PaarungBuilder paarung(String teamHeim, String teamGast) {
		return paarung(team(teamHeim, teamId(teamHeim)), team(teamGast, teamId(teamGast)));
	}

	private static PaarungBuilder paarung(String teamHeim, String teamGast, URI wappenHeim, URI wappenGast) {
		return Paarung.builder()
				.heim(entry(Team.builder().id(teamId(teamHeim)).name(teamHeim).wappen(wappenHeim).build()))
				.gast(entry(Team.builder().id(teamId(teamGast)).name(teamGast).wappen(wappenGast).build()));
	}

	private static EntryBuilder team(String team, TeamId teamId) {
		return Entry.builder().team(Team.builder().id(teamId).name(team)
				.kurzname(team == null ? null : team.substring(team.length() - 1)).build());
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
		sut = stream(this.paarungen).reduce(sut, Tabelle::add, (t1, t2) -> t1);
	}

	private void dannIstDieTabelle(String expected) {
		assertThat(print(sut.entries())).isEqualTo(line(stream(expected.split("\\|")).map(String::trim)));
	}

	@SafeVarargs
	private void dannIstDieTabelle(ThrowingConsumer<? super TabellenPlatz>... requirements) {
		assertThat(sut.entries()).satisfiesExactly(requirements);
	}

	private void dannIstDieTendenz(String team, Ergebnis... tendenz) {
		assertThat(tendenzForTeam(team).ergebnisse()).containsExactly(tendenz);
	}

	private Tendenz tendenzForTeam(String team) {
		var tabellenPlatz = sut.entries().stream().filter(t -> t.team().name().equals(team)).findFirst()
				.orElseThrow(() -> new IllegalStateException("No entry for team " + team));
		return tabellenPlatz.tendenz();
	}

	private static String print(List<TabellenPlatz> plaetze) {
		List<String> attribs = asList("platz", "verein", "kurzname", "spiele", "siege", "unentschieden", "niederlagen",
				"punkte", "gesamtTore", "gesamtGegentore", "torDifferenz");
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
		return attribs.stream().map(a -> {
			if (a.equals("verein")) {
				return platz.team().name();
			} else if (a.equals("kurzname")) {
				return platz.team().kurzname();
			} else {
				return readValue(platz, declaredMethods, a);
			}
		}).toList();
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
