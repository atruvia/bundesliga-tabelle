package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.Arrays.stream;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo.AvailableLeague;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultOpenLigaDbResultinfoRepo implements OpenLigaDbResultinfoRepo {

	private final RestTemplate restTemplate;
	private final AvailableLeagueRepo availableLeagueRepo;

	public List<Resultinfo> getResultinfos(String league, String season) {
		AvailableLeague availableLeague = availableLeagueRepo.getAvailableLeague(league, season).orElseThrow(
				() -> new AvailableLeagueNotFoundException(league, season));
		return getResultinfos(availableLeague.leagueId);
	}

	private List<Resultinfo> getResultinfos(int leagueId) {
		return stream(restTemplate.getForObject("https://api.openligadb.de/getresultinfos/{leagueId}",
				Resultinfo[].class, leagueId)).toList();
	}

}