package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.endergebnisType;
import static org.assertj.core.api.Assertions.assertThat;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class DefaultOpenLigaDbResultinfoRepoIT {

	RestClient restClient = new RestClient(new RestTemplate());
	OpenLigaDbResultinfoRepo sut = new DefaultOpenLigaDbResultinfoRepo(restClient,
			new AvailableLeagueRepo(restClient));

	@CartesianTest(name = "{index} => league: {0} season: {1}")
	void endergebnisHasHighestGlobalId( //
			@Values(strings = { "bl1", "bl2" }) String league, //
			@Values(strings = { "2022", "2023" }) String season //
	) {
		List<OpenligaDbResultinfo> results = sut.getResultinfos(league, season);
		assertThat(endergebnisType(results).name).isEqualTo("Endergebnis");
	}

}
