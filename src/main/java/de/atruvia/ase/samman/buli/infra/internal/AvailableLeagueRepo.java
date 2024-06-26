package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.util.Streams.toOnlyElement;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PUBLIC;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
public class AvailableLeagueRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getavailableleagues";

	@ToString
	@FieldDefaults(level = PUBLIC)
	static class AvailableLeague {
		int leagueId;
		String leagueShortcut;
		String leagueSeason;
	}

	private final RestTemplate restTemplate;

	public Optional<AvailableLeague> getAvailableLeague(String leagueShortcut, String leagueSeason) {
		return stream(restTemplate.getForObject(SERVICE_URI, AvailableLeague[].class)) //
				.filter(l -> leagueShortcut.equals(l.leagueShortcut)) //
				.filter(l -> leagueSeason.equals(l.leagueSeason)) //
				.reduce(toOnlyElement());
	}
}