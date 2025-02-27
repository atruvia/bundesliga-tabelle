package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.springframework.ResponseFromResourcesSupplier.responseFromResources;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restClient;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;
import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.GlobalResultInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class OldbSpieltagRepoMother {

	public static OldbSpieltagRepo spieltagFsRepo() {
		return new OldbSpieltagRepo(
				restClient(responseFromResources(
						p -> "getmatchdata/%s/%s.json".formatted(p[p.length - 2], p[p.length - 1]))),
				resultInfoProvider(2));
	}

	public static OldbTeamRepo teamFsRepo() {
		return new OldbTeamRepo(restClient(responseFromResources(
				p -> "getavailableteams/%s/%s.json".formatted(p[p.length - 2], p[p.length - 1]))));
	}

	public static OldbResultInfoRepo resultInfoProvider(int globalResultInfoId) {
		return (__league, __season) -> List.of(resultInfo(globalResultInfoId));
	}

	private static OldbResultInfo resultInfo(int globalResultInfoId) {
		OldbResultInfo resultInfo = new OldbResultInfo();
		resultInfo.orderId = 42;
		resultInfo.globalResultInfo = globalResultInfo(globalResultInfoId);
		return resultInfo;
	}

	private static GlobalResultInfo globalResultInfo(int globalResultInfoId) {
		GlobalResultInfo globalResultInfo = new GlobalResultInfo();
		globalResultInfo.id = globalResultInfoId;
		return globalResultInfo;
	}

}
