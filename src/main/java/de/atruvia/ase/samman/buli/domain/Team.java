package de.atruvia.ase.samman.buli.domain;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@Entity
public class Team {

	@Value
	@RequiredArgsConstructor(access = PRIVATE)
	public static class TeamIdentifier {
		String value;

		public static TeamIdentifier teamIdentifier(long value) {
			return new TeamIdentifier(String.valueOf(value));
		}

		public static TeamIdentifier teamIdentifier(String value) {
			return new TeamIdentifier(value);
		}
	}

	@Identity
	TeamIdentifier identifier;
	String name;
	URI wappen;

	public Team(TeamIdentifier identifier, String name, URI wappen) {
		this.identifier = identifier == null ? new TeamIdentifier(name) : identifier;
		this.name = name;
		this.wappen = wappen;
	}

}
