package de.atruvia.ase.samman.buli.infra.adapters.primary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.domain.TabellenPlatzMother.platzWith;
import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static de.atruvia.ase.samman.buli.infra.adapters.primary.TabellenHttpAdapter.TENDENZ_PATTERN;
import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz.TabellenPlatzBuilder;
import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueNotFoundException;

@WebMvcTest
@AutoConfigureMockMvc
class TabellenHttpAdapterTest {

	@Autowired
	TabellenHttpAdapter sut;

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	TabellenService tabellenService;

	@Test
	void shouldReturnDefaultMessage() throws Exception {
		String league = "bl1";
		String season = "2022";

		TabellenPlatz platz1 = platzWithBase(10, platzWith(SIEG, UNENTSCHIEDEN, NIEDERLAGE).toBuilder());
		TabellenPlatz platz2 = platzWithBase(20, platzWith().toBuilder());
		when(tabellenService.erstelleTabelle(league, season)).thenReturn(tabelleWithEntries(List.of(platz1, platz2)));

		// TODO Streng genommen testen wir hier auch wieder mehr als wir sollten, denn
		// wir testen hier auch wieder die TabellenPlatz::merge Funktionalität mit ab
		// und ob "int getTorDifferenz() { return getTore() - getGegentore(); }" richtig
		// ist.
		// Eigentlich sollte für TabellenPlatz ein Test-Double genutzt werden. Es muss
		// dann jedoch sichergestellt werden, dass die Reihenfolge der "ergebnisse" im
		// Test-Double bei S,U,N der Reihenfolge von TabellenPlatz::merge entspricht
		mockMvc.perform(get(format("/tabelle/%s/%s", league, season))) //
				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.[0].wappen", is(platz1.team().wappen().toASCIIString()))) //
				.andExpect(jsonPath("$.[0].team", is(platz1.team().name()))) //
				.andExpect(jsonPath("$.[0].shortName", is(platz1.team().kurzname()))) //
				.andExpect(jsonPath("$.[0].spiele", is(platz1.spiele()))) //
				.andExpect(jsonPath("$.[0].siege", is(platz1.siege()))) //
				.andExpect(jsonPath("$.[0].unentschieden", is(platz1.unentschieden()))) //
				.andExpect(jsonPath("$.[0].niederlagen", is(platz1.niederlagen()))) //
				.andExpect(jsonPath("$.[0].tore", is(platz1.gesamtTore()))) //
				.andExpect(jsonPath("$.[0].gegentore", is(platz1.gesamtGegentore()))) //
				.andExpect(jsonPath("$.[0].tordifferenz", is(platz1.torDifferenz()))) //
				.andExpect(jsonPath("$.[0].punkte", is(platz1.punkte()))) //
				.andExpect(jsonPath("$.[0].tendenz[0]", is("N"))) //
				.andExpect(jsonPath("$.[0].tendenz[1]", is("U"))) //
				.andExpect(jsonPath("$.[0].tendenz[2]", is("S"))) //
				.andExpect(jsonPath("$.[0].tendenz.length()", is(3))) //
				.andExpect(jsonPath("$.[0]*", not(hasKey("laufendesSpiel"))))
				//
				.andExpect(jsonPath("$.[1].wappen", is(platz2.team().wappen().toASCIIString()))) //
				.andExpect(jsonPath("$.[1].team", is(platz2.team().name()))) //
				.andExpect(jsonPath("$.[1].shortName", is(platz2.team().kurzname()))) //
				.andExpect(jsonPath("$.[1].spiele", is(platz2.spiele()))) //
				.andExpect(jsonPath("$.[1].siege", is(platz2.siege()))) //
				.andExpect(jsonPath("$.[1].unentschieden", is(platz2.unentschieden()))) //
				.andExpect(jsonPath("$.[1].niederlagen", is(platz2.niederlagen()))) //
				.andExpect(jsonPath("$.[1].tore", is(platz2.gesamtTore()))) //
				.andExpect(jsonPath("$.[1].gegentore", is(platz2.gesamtGegentore()))) //
				.andExpect(jsonPath("$.[1].tordifferenz", is(platz2.torDifferenz()))) //
				.andExpect(jsonPath("$.[1].punkte", is(platz2.punkte()))) //
				.andExpect(jsonPath("$.[1].tendenz.length()", is(0))) //
				.andExpect(jsonPath("$.[1]*", not(hasKey("laufendesSpiel")))) //
		;
	}

	@Test
	void failsWith404IfTableIsEmpty() throws Exception {
		String league = "bl1";
		String season = "2022";

		when(tabellenService.erstelleTabelle(league, season)).thenReturn(tabelleWithEntries(emptyList()));

		mockMvc.perform(get(format("/tabelle/%s/%s", league, season))) //
				.andDo(print()) //
				.andExpect(status().isNotFound()) //
		;
	}

	@Test
	void failsWith404WhenInvalidSeasonIsQueried() throws Exception {
		String validLeague = "bl1";
		String invalidSeason = "9999";

		when(tabellenService.erstelleTabelle(validLeague, invalidSeason))
				.thenThrow(new AvailableLeagueNotFoundException(validLeague, invalidSeason));

		mockMvc.perform(get(format("/tabelle/%s/%s", validLeague, invalidSeason))) //
				.andDo(print()) //
				.andExpect(status().isNotFound()) //
		;
	}

	@Test
	void allErgebnisTendenzesMatchTheTendenzPattern() throws Exception {
		String league = "bl1";
		String season = "2022";

		Ergebnis[] values = Ergebnis.values();
		TabellenPlatz platz1 = platzWithBase(10, platzWith(values).toBuilder());
		when(tabellenService.erstelleTabelle(league, season)).thenReturn(tabelleWithEntries(List.of(platz1)));

		String content = mockMvc.perform(get(format("/tabelle/%s/%s", league, season))) //
				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();
		List<String> tendenzStrings = new ObjectMapper().readTree(content).get(0).get("tendenz").valueStream()
				.map(JsonNode::asText).toList();
		assertThat(tendenzStrings).hasSameSizeAs(values).allSatisfy(s -> assertThat(s).matches(TENDENZ_PATTERN));
	}

	static Tabelle tabelleWithEntries(List<TabellenPlatz> result) {
		return new Tabelle() {
			@Override
			public Tabelle add(Paarung paarung) {
				throw new IllegalStateException("not supported");
			}

			@Override
			public List<TabellenPlatz> entries() {
				return result;
			}

		};
	}

	static TabellenPlatz platzWithBase(int base, TabellenPlatzBuilder builder) {
		int cnt = 0;
		return builder //
				.team(Team.builder().id(teamId("Identifier " + base + (++cnt))).name("Team " + base)
						.kurzname("T" + base).wappen(create("proto://wappen-team-" + base)).build()) //
				.spiele(base + (++cnt)) //
				.withTore(HEIM, base + (++cnt)) //
				.withGegentore(HEIM, base + (++cnt)) //
				.withTore(AUSWAERTS, base + (++cnt)) //
				.withGegentore(AUSWAERTS, base + (++cnt)) //
				.punkte(base + (++cnt)) //
				.build();
	}

}
