package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.endergebnisType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.springframework.web.client.RestTemplate;

class DefaultOpenLigaDbResultinfoRepoIT {

	RestTemplate restTemplate = new RestTemplate();
	OpenLigaDbResultinfoRepo sut = new DefaultOpenLigaDbResultinfoRepo(restTemplate,
			new AvailableLeagueRepo(restTemplate));

	@CartesianTest
	void endergebnisHasHighestGlobalId( //
			@Values(strings = { "bl1", "bl2" }) String league, //
			@Values(strings = { "2022", "2023" }) String season //
	) {
		assertThat(endergebnisType(sut.getResultinfos(league, season)).name).isEqualTo("Endergebnis");
	}

}
