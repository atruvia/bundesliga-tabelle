package de.atruvia.ase.samman.buli.infra.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AvailableLeagueNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4994065085648695879L;

	// TODO verify if present in test
	private final String league;
	// TODO verify if present in test
	private final String season;

	public AvailableLeagueNotFoundException(String league, String season) {
		super("League %s, season %s not found".formatted(league, season));
		this.league = league;
		this.season = season;
	}

}
