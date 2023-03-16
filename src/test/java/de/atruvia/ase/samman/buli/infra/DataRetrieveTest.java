package de.atruvia.ase.samman.buli.infra;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import de.atruvia.ase.sammanbuli.domain.Paarung;
import de.atruvia.ase.sammanbuli.domain.Paarung.PaarungBuilder;
import lombok.ToString;

class DataRetrieveTest {

	@ToString
	class Team {
		String teamName;
	}

	@ToString
	class MatchResult {
		int pointsTeam1;
		int pointsTeam2;
	}

	@ToString
	class Match {
		Team team1;
		Team team2;
		MatchResult[] matchResults;

		Paarung toDomain() {
			PaarungBuilder b = Paarung.builder().team1(team1.teamName).team2(team2.teamName);
			b = matchResults.length == 0 ? b : b.ergebnis(matchResults[0].pointsTeam1, matchResults[0].pointsTeam2);
			return b.build();
		}
	}

	

	

}
