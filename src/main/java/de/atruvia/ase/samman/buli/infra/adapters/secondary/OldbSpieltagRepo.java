package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.GEPLANT;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.LAUFEND;
import static de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder.paarung;
import static de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.endergebnisType;
import static de.atruvia.ase.samman.buli.util.Streams.toOnlyElement;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import de.atruvia.ase.samman.buli.domain.ports.secondary.SpieltagRepo;
import de.atruvia.ase.samman.buli.infra.adapters.secondary.OldbTeamRepo.OldbTeam;
import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/** Provides read access to match days from the Open Liga DB. */
@Repository
@RequiredArgsConstructor
@SecondaryAdapter
public class OldbSpieltagRepo implements SpieltagRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getmatchdata/{league}/{season}";

	private final RestClient restClient;
	private final OldbResultInfoRepo resultInfoRepo;

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class OldbMatchResult {
		int resultTypeID;
		int pointsTeam1;
		int pointsTeam2;

		private static Optional<OldbMatchResult> endergebnisOf(Stream<OldbMatchResult> matchResults,
															   List<OldbResultInfo> resultInfos) {
			int endergebnisResultTypeId = endergebnisType(resultInfos).globalResultInfo.id;
			return matchResults.filter(t -> t.resultTypeID == endergebnisResultTypeId).reduce(toOnlyElement());
		}

	}

	@ToString
	@FieldDefaults(level = PUBLIC)
	@Getter(value = PRIVATE)
	@Accessors(fluent = true)
	@SecondaryAdapter
	private static class Goal {

		private static final Goal NULL = new Goal();
		private static final Comparator<Goal> compareById = comparing(Goal::goalID);
		private static final Comparator<Goal> compareByMinuteThenById = comparing(Goal::matchMinute)
				.thenComparing(compareById);

		private static Goal lastGoalOf(Goal[] goals) {
			var goalsWithMinutes = goalsWithMinuteAttribute(goals).toList();
			assert orderedByIdMatchesOrderedByMinute(goals, goalsWithMinutes)
					: "goals sorted by id not in same order like sorted by minute: " + Arrays.toString(goals);
			boolean allGoalsHaveMinutes = goalsWithMinutes.size() == goals.length;
			var comparator = allGoalsHaveMinutes ? compareByMinuteThenById : compareById;
			return stream(goals).max(comparator).orElse(NULL);
		}

		private static Stream<Goal> goalsWithMinuteAttribute(Goal[] goals) {
			return stream(goals).filter(g -> nonNull(g.matchMinute()));
		}

		private static boolean orderedByIdMatchesOrderedByMinute(Goal[] goals, List<Goal> goalsWithMinutes) {
			var byId = goalsWithMinutes.stream().sorted(compareById).toList();
			var byMinuteThenById = goalsWithMinutes.stream().sorted(compareByMinuteThenById).toList();
			return byId.equals(byMinuteThenById);
		}

		int goalID;
		int scoreTeam1;
		int scoreTeam2;
		/**
		 * Field is optional, not present in any case/league, so we depend on the
		 * {@link #goalID} rather than the {@link #matchMinute} for sorting. Since there
		 * is no contract/guarantee that {@link #goalID} are in chronological ordered we
		 * added the assert to verify the assumption, that the IDs are sorted
		 * chronologically.
		 */
		Integer matchMinute;

	}

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	private static class OldbMatch {
		OldbTeam team1;
		OldbTeam team2;
		boolean matchIsFinished;
		OldbMatchResult[] matchResults;
		Goal[] goals;

		private Paarung toDomain(List<OldbResultInfo> resultInfos) {
			var ergebnisTyp = ergebnisTyp();
			var paarung = paarung(team1.toDomain(), team2.toDomain()).ergebnisTyp(ergebnisTyp);
			if (ergebnisTyp == BEENDET) {
				var endergebnis = OldbMatchResult.endergebnisOf(stream(matchResults), resultInfos)
						.orElseThrow(() -> new IllegalStateException("No final result found in finished game " + this));
				paarung = paarung.goals(endergebnis.pointsTeam1, endergebnis.pointsTeam2);
			} else if (ergebnisTyp == LAUFEND) {
				// a final result is always present on started games, but in some cases it has
				// been 0:0 while there have already been shot some goals. Of course, we always
				// could take the "goals" in account (this always is correct) but we should
				// prefer using the final result if it's present.
				// In the meanwhile we have seen everything at started games! e.g. a half-time
				// score of 3:2 with a final score of 0:0 and goals where missing (0:1, 0:3)
				var lastGoal = Goal.lastGoalOf(goals);
				paarung = paarung.goals(lastGoal.scoreTeam1, lastGoal.scoreTeam2);
			}
			return paarung.build();
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
		List<OldbResultInfo> resultInfos = resultInfoRepo.getResultInfos(league, season);
		OldbMatch[] matches = restClient.get(SERVICE_URI, OldbMatch[].class, league, season);
		return stream(matches).map(t -> t.toDomain(resultInfos)).toList();
	}

}
