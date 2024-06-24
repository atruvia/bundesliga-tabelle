package de.atruvia.ase.samman.buli.domain;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jmolecules.ddd.types.Identifier;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@ValueObject
public class Team {

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

}
