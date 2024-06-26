package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.Arrays.asList;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo.AvailableLeague;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultOpenLigaDbResultinfoRepo implements OpenLigaDbResultinfoRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getresultinfos/{leagueId}";

	private final RestTemplate restTemplate;
	private final AvailableLeagueRepo availableLeagueRepo;

	public List<OpenligaDbResultinfo> getResultinfos(String league, String season) {
		AvailableLeague availableLeague = availableLeagueRepo.getAvailableLeague(league, season)
				.orElseThrow(() -> new AvailableLeagueNotFoundException(league, season));
		return getResultinfos(availableLeague.leagueId);
	}

	private List<OpenligaDbResultinfo> getResultinfos(int leagueId) {
		return asList(restTemplate.getForObject(SERVICE_URI, OpenligaDbResultinfo[].class, leagueId));
	}

}