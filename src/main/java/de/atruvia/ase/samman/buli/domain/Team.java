package de.atruvia.ase.samman.buli.domain;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@Builder
@With
@ValueObject
public class Team {

	@Value
	@RequiredArgsConstructor(access = PRIVATE)
	public static class TeamId {

		@NonNull
		String value;

		public static TeamId teamId(long value) {
			return teamId(String.valueOf(value));
		}

		public static TeamId teamId(String value) {
			return new TeamId(value);
		}
	}

	@NonNull
	TeamId id;
	String name;
	String kurzname;
	URI wappen;

	public Team(@NonNull TeamId id, String name, String kurzname, URI wappen) {
		this.id = id;
		this.name = name;
		this.kurzname = kurzname == null || kurzname.isEmpty() || strlen(name) < strlen(kurzname) ? name : kurzname;
		this.wappen = wappen;
	}

	private static int strlen(String string) {
		return string == null ? 0 : string.length();
	}

}
