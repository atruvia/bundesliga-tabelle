package de.atruvia.ase.samman.buli.infra.internal;

import static de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.endergebnisType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo;
import de.atruvia.ase.samman.buli.infra.internal.OpenLigaDbResultinfoRepo.OpenligaDbResultinfo.GlobalResultInfo;

class OpenLigaDbResultinfoRepoTest {

	@Test
	void testEndergebnisTypeIsHighestGlobalResultInfo() {
		OpenligaDbResultinfo r1 = resultinfo(1);
		OpenligaDbResultinfo r2 = resultinfo(2);
		assertThat(endergebnisType(List.of(r2, r1))).isSameAs(r2);
	}

	static OpenligaDbResultinfo resultinfo(int globalResultInfoId) {
		OpenligaDbResultinfo resultinfo = new OpenligaDbResultinfo();
		resultinfo.globalResultInfo = globalResultInfo(globalResultInfoId);
		return resultinfo;
	}

	static GlobalResultInfo globalResultInfo(int globalResultInfoId) {
		GlobalResultInfo globalResultInfo = new GlobalResultInfo();
		globalResultInfo.id = globalResultInfoId;
		return globalResultInfo;
	}

}
