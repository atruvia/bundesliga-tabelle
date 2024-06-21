package de.atruvia.ase.samman.buli.domain.ports.secondary;

import java.util.List;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import de.atruvia.ase.samman.buli.domain.Team;

@SecondaryPort
@Repository
public interface TeamRepo {

	List<Team> getTeams(String league, String season);

}
