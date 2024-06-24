package de.atruvia.ase.samman.buli.domain;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import de.atruvia.ase.samman.buli.domain.Team.TeamId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@Entity
public class Team implements AggregateRoot<Team, TeamId> {

	@Value
	@RequiredArgsConstructor(access = PRIVATE)
	public static class TeamId implements Identifier {

		String value;

		public static TeamId teamId(long value) {
			return teamId(String.valueOf(value));
		}

		public static TeamId teamId(String value) {
			return new TeamId(value);
		}
	}

	TeamId id;
	String name;
	URI wappen;

	public Team(TeamId id, String name, URI wappen) {
		this.id = id == null ? new TeamId(name) : id;
		this.name = name;
		this.wappen = wappen;
	}

	@Override
	public TeamId getId() {
		return id;
	}

}
