package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.endergebnisType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo;
import de.atruvia.ase.samman.buli.infra.internal.OldbResultInfoRepo.OldbResultInfo.GlobalResultInfo;

class OldbResultInfoRepoTest {

	@Test
	void testEndergebnisTypeIsHighestGlobalResultInfo() {
		OldbResultInfo r1 = resultInfo(1);
		OldbResultInfo r2 = resultInfo(2);
		assertThat(endergebnisType(List.of(r2, r1))).isSameAs(r2);
	}

	static OldbResultInfo resultInfo(int globalResultInfoId) {
		OldbResultInfo resultInfo = new OldbResultInfo();
		resultInfo.globalResultInfo = globalResultInfo(globalResultInfoId);
		return resultInfo;
	}

	static GlobalResultInfo globalResultInfo(int globalResultInfoId) {
		GlobalResultInfo globalResultInfo = new GlobalResultInfo();
		globalResultInfo.id = globalResultInfoId;
		return globalResultInfo;
	}

}
