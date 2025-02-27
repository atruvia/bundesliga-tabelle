package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.util.Streams.toOnlyElement;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PUBLIC;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
public class AvailableLeagueRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getavailableleagues";

	@ToString
	@FieldDefaults(level = PUBLIC)
	public static class AvailableLeague {
		int leagueId;
		String leagueShortcut;
		String leagueSeason;
	}

	private final RestClient restClient;

	public Optional<AvailableLeague> getAvailableLeague(String leagueShortcut, String leagueSeason) {
		AvailableLeague[] availableLeagues = restClient.get(SERVICE_URI, AvailableLeague[].class);
		return stream(availableLeagues) //
				.filter(l -> leagueShortcut.equals(l.leagueShortcut)) //
				.filter(l -> leagueSeason.equals(l.leagueSeason)) //
				.reduce(toOnlyElement());
	}
}