package de.atruvia.ase.samman.buli.infra.adapters.primary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.domain.TabellenPlatzMother.platzWith;
import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Collections;
import java.util.List;

import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz.TabellenPlatzBuilder;
import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;

@SpringBootTest
@AutoConfigureMockMvc
class TabellenHttpAdapterTest {

	// TODO MockMvc fails without explicitly setting an ExceptionHandler (which is
	// there when running the application so it seems, that we are testing our
	// "mock" here) :/
	@ControllerAdvice
	static class GlobalExceptionHandler {

		@ExceptionHandler(Exception.class)
		public ResponseEntity<String> handleException(Exception e) {
			return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Autowired
	TabellenHttpAdapter sut;

	MockMvc mockMvc;
	MockMvc mockMvcForInternalServerError;

	@BeforeEach
	void setup() {
		mockMvc = standaloneSetup(sut).build();
		mockMvcForInternalServerError = standaloneSetup(sut).setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@MockBean
	TabellenService tabellenService;

	@Test
	void shouldReturnDefaultMessage() throws Exception {
		String league = "bl1";
		String season = "2022";

		TabellenPlatz platz1 = platzWithBase(10, platzWith(SIEG, UNENTSCHIEDEN, NIEDERLAGE).toBuilder());
		TabellenPlatz platz2 = platzWithBase(20, platzWith().toBuilder());
		when(tabellenService.erstelleTabelle(league, season)).thenReturn(List.of(platz1, platz2));

		// TODO Streng genommen testen wir hier auch wieder mehr als wir sollten, denn
		// wir testen hier auch wieder die TabellenPlatz::merge Funktionalität mit ab
		// und ob "int getTorDifferenz() { return getTore() - getGegentore(); }" richtig
		// ist.
		// Eigentlich sollte für TabellenPlatz ein Test-Double genutzt werden. Es muss
		// dann jedoch sichergestellt werden, dass die Reihenfolge der "ergebnisse" im
		// Test-Double bei S,U,N der Reihenfolge von TabellenPlatz::merge entspricht
		mockMvc.perform(get("/tabelle/" + league + "/" + season)) //
				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.[0].wappen", is(platz1.wappen().toASCIIString()))) //
				.andExpect(jsonPath("$.[0].team", is(platz1.teamName()))) //
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
				.andExpect(jsonPath("$.[1].wappen", is(platz2.wappen().toASCIIString()))) //
				.andExpect(jsonPath("$.[1].team", is(platz2.teamName()))) //
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

		when(tabellenService.erstelleTabelle(league, season)).thenReturn(emptyList());

		mockMvc.perform(get("/tabelle/" + league + "/" + season)) //
				.andDo(print()) //
				.andExpect(status().isNotFound()) //
		;
	}

	@Test
	void failsWith500WhenServiceThrowsException() throws Exception {
		String league = "bl1";
		String season = "2022";

		String message = "Some service exception";
		when(tabellenService.erstelleTabelle(league, season)).thenThrow(new RuntimeException(message));

		mockMvcForInternalServerError.perform(get("/tabelle/" + league + "/" + season)) //
				.andDo(print()) //
				.andExpect(status().is5xxServerError()) //
		;
	}

	@Test
	void failsWith404WhenInvalidSeasonIsQueried() throws Exception {
		String validLeague = "bl1";
		String invalidSeason = "9999";

		when(tabellenService.erstelleTabelle(validLeague, invalidSeason))
				.thenThrow(new AvailableLeagueNotFoundException(validLeague, invalidSeason));

		mockMvc.perform(get("/tabelle/" + validLeague + "/" + invalidSeason)) //
				.andDo(print()) //
				.andExpect(status().isNotFound()) //
		;
	}

	static TabellenPlatz platzWithBase(int base, TabellenPlatzBuilder builder) {
		int cnt = 0;
		return builder.wappen(create("proto://wappen-team-" + base)) //
				.team("Team " + base, "Identifier " + base + (++cnt)) //
				.spiele(base + (++cnt)) //
				.withTore(HEIM, base + (++cnt)) //
				.withGegentore(HEIM, base + (++cnt)) //
				.withTore(AUSWAERTS, base + (++cnt)) //
				.withGegentore(AUSWAERTS, base + (++cnt)) //
				.punkte(base + (++cnt)) //
				.build();
	}

}
