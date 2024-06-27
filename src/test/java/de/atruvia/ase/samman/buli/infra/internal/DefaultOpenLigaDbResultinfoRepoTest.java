package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.endergebnisType;
import static de.atruvia.ase.samman.buli.springframework.ResponseFromResourcesSupplier.responseFromResources;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo;

class DefaultOpenLigaDbResultinfoRepoTest {

	static final String ENDERGEBNIS = "Endergebnis";

	OpenLigaDbResultinfoRepo sut = new DefaultOpenLigaDbResultinfoRepo(
			restClient(responseFromResources(p -> "getresultinfos/%s.json".formatted(p[p.length - 1]))),
			availableLeagueRepo());

	AvailableLeagueRepo availableLeagueRepo() {
		return new AvailableLeagueRepo(
				restClient(responseFromResources(__ -> "getavailableleagues/getavailableleagues.json")));
	}

	@Test
	void endergebnisType2022() {
		List<OpenligaDbResultinfo> resultinfos = sut.getResultinfos("bl1", "2022");
		assertSoftly(s -> assertThat(resultinfos).satisfiesExactly( //
				r -> {
					s.assertThat(r).isSameAs(endergebnisType(resultinfos));
					s.assertThat(r.id).isEqualTo(5337);
					s.assertThat(r.name).isEqualTo(ENDERGEBNIS);
				}, r -> {
					s.assertThat(r.id).isEqualTo(5338);
					s.assertThat(r.name).isEqualTo("Halbzeit");
				}));
	}

	@Test
	void endergebnisType2023() {
		List<OpenligaDbResultinfo> resultinfos = sut.getResultinfos("bl1", "2023");
		assertSoftly(s -> assertThat(resultinfos).satisfiesExactly( //
				r -> {
					s.assertThat(r).isSameAs(endergebnisType(resultinfos));
					s.assertThat(r.id).isEqualTo(5413);
					s.assertThat(r.name).isEqualTo(ENDERGEBNIS);
				}, r -> {
					s.assertThat(r.id).isEqualTo(5456);
					s.assertThat(r.name).isEqualTo("Halbzeitergebnis");
				}));

	}

	@Test
	void runtimeExceptionOnUnknownLeague() {
		assertThatThrownBy(() -> sut.getResultinfos("XXX", "2023")).hasMessageContainingAll("XXX", "2023", "not found");
	}

	@Test
	void runtimeExceptionOnUnknownSeason() {
		assertThatThrownBy(() -> sut.getResultinfos("bl1", "0000")).hasMessageContainingAll("bl1", "0000", "not found");
	}

}
