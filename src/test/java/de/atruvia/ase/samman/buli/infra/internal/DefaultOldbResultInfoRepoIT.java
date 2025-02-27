package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.endergebnisType;
import static org.assertj.core.api.Assertions.assertThat;

import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class DefaultOldbResultInfoRepoIT {

	RestClient restClient = new RestClient(new RestTemplate());
	OldbResultInfoRepo sut = new DefaultOldbResultInfoRepo(restClient,
			new AvailableLeagueRepo(restClient));

	@CartesianTest
	void endergebnisHasHighestGlobalId( //
			@Values(strings = { "bl1", "bl2" }) String league, //
			@Values(strings = { "2022", "2023" }) String season //
	) {
		List<OldbResultInfo> results = sut.getResultInfos(league, season);
		assertThat(endergebnisType(results).name).isEqualTo("Endergebnis");
	}

}
