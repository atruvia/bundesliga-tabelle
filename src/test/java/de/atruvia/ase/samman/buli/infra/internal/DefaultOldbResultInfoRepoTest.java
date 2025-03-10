package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.endergebnisType;
import static de.atruvia.ase.samman.buli.springframework.ResponseFromResourcesSupplier.responseFromResources;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;

class DefaultOldbResultInfoRepoTest {

	static final String ENDERGEBNIS = "Endergebnis";

	OldbResultInfoRepo sut = new DefaultOldbResultInfoRepo(
			restClient(responseFromResources(p -> "getresultinfos/%s.json".formatted(p[p.length - 1]))),
			availableLeagueRepo());

	AvailableLeagueRepo availableLeagueRepo() {
		return new AvailableLeagueRepo(
				restClient(responseFromResources(__ -> "getavailableleagues/getavailableleagues.json")));
	}

	@Test
	void endergebnisType2022() {
		List<OldbResultInfo> resultInfos = sut.getResultInfos("bl1", "2022");
		assertSoftly(s -> assertThat(resultInfos).satisfiesExactly( //
				r -> {
					s.assertThat(r).isSameAs(endergebnisType(resultInfos));
					s.assertThat(r.id).isEqualTo(5337);
					s.assertThat(r.name).isEqualTo(ENDERGEBNIS);
				}, r -> {
					s.assertThat(r.id).isEqualTo(5338);
					s.assertThat(r.name).isEqualTo("Halbzeit");
				}));
	}

	@Test
	void endergebnisType2023() {
		List<OldbResultInfo> resultInfos = sut.getResultInfos("bl1", "2023");
		assertSoftly(s -> assertThat(resultInfos).satisfiesExactly( //
				r -> {
					s.assertThat(r).isSameAs(endergebnisType(resultInfos));
					s.assertThat(r.id).isEqualTo(5413);
					s.assertThat(r.name).isEqualTo(ENDERGEBNIS);
				}, r -> {
					s.assertThat(r.id).isEqualTo(5456);
					s.assertThat(r.name).isEqualTo("Halbzeitergebnis");
				}));

	}

	@Test
	void runtimeExceptionOnUnknownLeague() {
		AvailableLeagueNotFoundException ex = catchThrowableOfType( //
				() -> sut.getResultInfos("XXX", "2023"), //
				AvailableLeagueNotFoundException.class //
		);
		assertThat(ex.getLeague()).isEqualTo("XXX");
		assertThat(ex.getSeason()).isEqualTo("2023");
		assertThat(ex).hasMessageContainingAll("XXX", "2023", "not found");
	}

	@Test
	void runtimeExceptionOnUnknownSeason() {
		AvailableLeagueNotFoundException ex = catchThrowableOfType( //
				() -> sut.getResultInfos("bl1", "0000"), //
				AvailableLeagueNotFoundException.class //
		);
		assertThat(ex.getLeague()).isEqualTo("bl1");
		assertThat(ex.getSeason()).isEqualTo("0000");
		assertThat(ex).hasMessageContainingAll("bl1", "0000", "not found");
	}

}
