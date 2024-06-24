package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.springframework.ResponseFromResourcesSupplier.responseFromResources;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restTemplateMock;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo;
import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.GlobalResultInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class OpenLigaDbSpieltagRepoMother {

	public static OpenLigaDbSpieltagRepo spieltagFsRepo() {
		return new OpenLigaDbSpieltagRepo(
				restTemplateMock(responseFromResources(
						p -> "getmatchdata/%s/%s.json".formatted(p[p.length - 2], p[p.length - 1]))),
				resultinfoProvider(2));
	}

	public static OpenLigaDbTeamRepo teamFsRepo() {
		return new OpenLigaDbTeamRepo(restTemplateMock(responseFromResources(
				p -> "getavailableteams/%s/%s.json".formatted(p[p.length - 2], p[p.length - 1]))));
	}

	public static OpenLigaDbResultinfoRepo resultinfoProvider(int globalResultInfoId) {
		return (__league, __season) -> List.of(resultinfo(globalResultInfoId));
	}

	private static OpenligaDbResultinfo resultinfo(int globalResultInfoId) {
		OpenligaDbResultinfo resultinfo = new OpenligaDbResultinfo();
		resultinfo.orderId = 42;
		resultinfo.globalResultInfo = globalResultInfo(globalResultInfoId);
		return resultinfo;
	}

	private static GlobalResultInfo globalResultInfo(int globalResultInfoId) {
		GlobalResultInfo globalResultInfo = new GlobalResultInfo();
		globalResultInfo.id = globalResultInfoId;
		return globalResultInfo;
	}

}
