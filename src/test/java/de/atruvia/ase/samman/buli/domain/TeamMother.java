package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static java.net.URI.create;

import de.atruvia.ase.samman.buli.domain.Team.TeamId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamMother {

	public static final TeamId idMuenchen = teamId(40);
	public static final TeamId idFrankfurt = teamId(91);
	public static final TeamId idBremen = teamId(134);

	public static final Team teamMuenchen = Team.builder().id(idMuenchen).name("FC Bayern München")
			.wappen(create("https://i.imgur.com/jJEsJrj.png")).build();

	public static final Team teamFrankfurt = Team.builder().id(idFrankfurt).name("Eintracht Frankfurt")
			.wappen(create("https://i.imgur.com/X8NFkOb.png")).build();

	public static final Team teamBremen = Team.builder().id(idBremen).name("Werder Bremen").wappen(create(
			"https://upload.wikimedia.org/wikipedia/commons/thumb/b/be/SV-Werder-Bremen-Logo.svg/681px-SV-Werder-Bremen-Logo.svg.png"))
			.build();

}
