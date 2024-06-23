package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static java.net.URI.create;

import de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamMother {

	public static final TeamIdentifier idMuenchen = teamIdentifier(40);
	public static final TeamIdentifier idFrankfurt = teamIdentifier(91);
	public static final TeamIdentifier idBremen = teamIdentifier(134);

	public static final Team teamMuenchen = Team.builder().identifier(idMuenchen).name("FC Bayern München")
			.wappen(create("https://i.imgur.com/jJEsJrj.png")).build();

	public static final Team teamFrankfurt = Team.builder().identifier(idFrankfurt).name("Eintracht Frankfurt")
			.wappen(create("https://i.imgur.com/X8NFkOb.png")).build();

	public static final Team teamBremen = Team.builder().identifier(idBremen).name("Werder Bremen").wappen(create(
			"https://upload.wikimedia.org/wikipedia/commons/thumb/b/be/SV-Werder-Bremen-Logo.svg/681px-SV-Werder-Bremen-Logo.svg.png"))
			.build();

}
