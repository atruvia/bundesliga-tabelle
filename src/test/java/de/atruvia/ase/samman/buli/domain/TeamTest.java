package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class TeamTest {

	@ParameterizedTest
	@NullAndEmptySource
	void doesUseLongnameAsShortnameIfShortnameIsAbsent(String kurzname) {
		Team team = Team.builder().id(teamId(42)).name("anyName").kurzname(kurzname).build();
		assertThat(team.kurzname()).isEqualTo(team.name());
	}

	@Test
	void ifShortnameIsLongerThanNameThenTheNameIsUsed() {
		Team team = Team.builder().id(teamId(42)).name("12345").kurzname("123456").build();
		assertThat(team.kurzname()).isEqualTo(team.name());
	}

	@Test
	void ifNameIsNullAndShortnameIsAbsentThenShortnameIsNullAsWell() {
		Team team = Team.builder().id(teamId(42)).name(null).kurzname(null).build();
		assertSoftly(s -> {
			s.assertThat(team.name()).isNull();
			s.assertThat(team.kurzname()).isNull();
		});
	}

}
