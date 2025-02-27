package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.Arrays.asList;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo.AvailableLeague;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultOldbResultInfoRepo implements OldbResultInfoRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getresultinfos/{leagueId}";

	private final RestClient restClient;
	private final AvailableLeagueRepo availableLeagueRepo;

	public List<OldbResultInfo> getResultInfos(String league, String season) {
		AvailableLeague availableLeague = availableLeagueRepo.getAvailableLeague(league, season)
				.orElseThrow(() -> new AvailableLeagueNotFoundException(league, season));
		return getResultInfos(availableLeague.leagueId);
	}

	private List<OldbResultInfo> getResultInfos(int leagueId) {
		OldbResultInfo[] results = restClient.get(SERVICE_URI, OldbResultInfo[].class, leagueId);
		return asList(results);
	}

}