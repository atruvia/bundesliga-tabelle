package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.GEPLANT;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.LAUFEND;
import static de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.Resultinfo.endergebnisType;
import static de.atruvia.ase.samman.buli.util.Streams.toOnlyElement;
import static java.net.URI.create;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static lombok.AccessLevel.PUBLIC;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder;
import de.atruvia.ase.samman.buli.domain.ports.secondary.SpieltagRepo;
import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.Resultinfo;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@SecondaryAdapter
public class OpenLigaDbSpieltagRepo implements SpieltagRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getmatchdata/{league}/{season}";

	private final RestTemplate restTemplate;
	private final OpenLigaDbResultinfoRepo resultinfoRepo;

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class Team {
		Object teamId;
		String teamName;
		String teamIconUrl;

		private Entry toDomain() {
			return new Entry(teamId, teamName, create(teamIconUrl), 0);
		}
	}

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class MatchResult {
		int resultTypeID;
		int pointsTeam1;
		int pointsTeam2;

		private static Optional<MatchResult> endergebnisOf(Stream<MatchResult> matchResults,
				List<Resultinfo> resultinfos) {
			int endergebnisResultTypeId = endergebnisType(resultinfos).globalResultInfo.id;
			return matchResults.filter(t -> t.resultTypeID == endergebnisResultTypeId).reduce(toOnlyElement());
		}

	}

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class Goal {

		private static final Comparator<Goal> inChronologicalOrder = comparing(g -> g.goalID);
		private static final Goal NULL = new Goal();

		private static Goal lastGoalOf(Goal[] goals) {
			return stream(goals).max(inChronologicalOrder).orElse(NULL);
		}

		int goalID;
		int scoreTeam1;
		int scoreTeam2;

	}

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class Match {
		Team team1;
		Team team2;
		boolean matchIsFinished;
		MatchResult[] matchResults;
		Goal[] goals;

		private Paarung toDomain(List<Resultinfo> resultinfos) {
			ErgebnisTyp ergebnisTyp = ergebnisTyp();
			PaarungBuilder builder = Paarung.builder().ergebnisTyp(ergebnisTyp) //
					.heim(team1.toDomain()).gast(team2.toDomain());
			if (ergebnisTyp == BEENDET) {
				MatchResult endergebnis = MatchResult.endergebnisOf(stream(matchResults), resultinfos)
						.orElseThrow(() -> new IllegalStateException("No final result found in finished game " + this));
				builder = builder.goals(endergebnis.pointsTeam1, endergebnis.pointsTeam2);
			} else if (ergebnisTyp == LAUFEND) {
				// a final result is always present on started games, but in some cases it has
				// been 0:0 while there have already been shoot some goals. Of course we always
				// could take the "goals" in account (this always is correct) but we should
				// prefer using the final result if it's present.
				// In the meanwhile we have seen everything at started games! e.g. a half time
				// score of 3:2 with a final score of 0:0 and goals where goals where missing
				// (0:1, 0:3)
				Goal lastGoal = Goal.lastGoalOf(goals);
				builder = builder.goals(lastGoal.scoreTeam1, lastGoal.scoreTeam2);
			}
			return builder.build();
		}

		private ErgebnisTyp ergebnisTyp() {
			if (matchIsFinished) {
				return BEENDET;
			} else if (matchResults.length > 0) {
				return LAUFEND;
			} else {
				return GEPLANT;
			}
		}

	}

	@Override
	public List<Paarung> lade(String league, String season) {
		List<Resultinfo> resultinfos = resultinfoRepo.getResultinfos(league, season);
		return stream(restTemplate.getForObject(SERVICE_URI, Match[].class, league, season))
				.map(t -> t.toDomain(resultinfos)).toList();
	}

}
