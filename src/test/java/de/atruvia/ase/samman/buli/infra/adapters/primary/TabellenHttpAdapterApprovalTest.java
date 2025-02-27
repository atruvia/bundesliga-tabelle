package de.atruvia.ase.samman.buli.infra.adapters.primary;

import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OldbSpieltagRepoMother.spieltagFsRepo;
import static org.approvaltests.JsonApprovals.verifyJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import de.atruvia.ase.samman.buli.domain.ports.primary.DefaultTabellenService;
import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;

@WebMvcTest
@AutoConfigureMockMvc
@Import(TabellenHttpAdapterApprovalTest.OldbConfig.class)
class TabellenHttpAdapterApprovalTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void approveWithRunningGames() throws Exception {
		String league = "bl1";
		String season = "2023-games-running";
		String jsonResponse = mockMvc.perform(get("/tabelle/" + league + "/" + season)) //
				.andDo(print()) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();
		verifyJson(jsonResponse);
	}

	@TestConfiguration
	static class OldbConfig {
		@Bean
		TabellenService tabellenService() {
			return new DefaultTabellenService(spieltagFsRepo());
		}
	}
}
