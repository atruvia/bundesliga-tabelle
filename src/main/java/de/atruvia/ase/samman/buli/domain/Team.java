package de.atruvia.ase.samman.buli.domain;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@Entity
public class Team implements AggregateRoot<Team, TeamIdentifier> {

	@Value
	@RequiredArgsConstructor(access = PRIVATE)
	public static class TeamIdentifier implements Identifier {

		String value;

		public static TeamIdentifier teamIdentifier(long value) {
			return new TeamIdentifier(String.valueOf(value));
		}

		public static TeamIdentifier teamIdentifier(String value) {
			return new TeamIdentifier(value);
		}
	}

	TeamIdentifier identifier;
	String name;
	URI wappen;

	public Team(TeamIdentifier identifier, String name, URI wappen) {
		this.identifier = identifier == null ? new TeamIdentifier(name) : identifier;
		this.name = name;
		this.wappen = wappen;
	}

	@Override
	public TeamIdentifier getId() {
		return identifier;
	}

}
