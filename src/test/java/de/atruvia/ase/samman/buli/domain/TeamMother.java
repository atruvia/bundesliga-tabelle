package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static java.net.URI.create;

import java.net.URI;

import de.atruvia.ase.samman.buli.domain.Team.TeamId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamMother {

	public static final TeamId idDortmund = teamId(7);
	public static final TeamId idMuenchen = teamId(40);
	public static final TeamId idFrankfurt = teamId(91);
	public static final TeamId idBremen = teamId(134);

	public static Team anyTeam = Team.builder().id(teamId("anyIdentifier")).name("team1")
			.wappen(URI.create("proto://wappen1")).build();

	public static final Team teamDortmund = Team.builder().id(idDortmund).name("Borussia Dortmund").wappen(create(
			"https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Borussia_Dortmund_logo.svg/560px-Borussia_Dortmund_logo.svg.png"))
			.build();

	public static final Team teamMuenchen = Team.builder().id(idMuenchen).name("FC Bayern München")
			.wappen(create("https://upload.wikimedia.org/wikipedia/commons/1/1f/Logo_FC_Bayern_M%C3%BCnchen_%282002%E2%80%932017%29.svg")).build();

	public static final Team teamFrankfurt = Team.builder().id(idFrankfurt).name("Eintracht Frankfurt")
			.wappen(create("https://i.imgur.com/X8NFkOb.png")).build();

	public static final Team teamBremen = Team.builder().id(idBremen).name("Werder Bremen").wappen(create(
			"https://upload.wikimedia.org/wikipedia/commons/thumb/b/be/SV-Werder-Bremen-Logo.svg/681px-SV-Werder-Bremen-Logo.svg.png"))
			.build();

}
