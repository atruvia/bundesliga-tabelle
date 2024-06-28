package de.atruvia.ase.samman.buli.infra.adapters.primary;

import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static org.approvaltests.JsonApprovals.verifyJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import de.atruvia.ase.samman.buli.domain.ports.primary.DefaultTabellenService;
import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;

@SpringBootTest
@AutoConfigureMockMvc
class TabellenHttpAdapterApprovalTest {

	@Autowired
	TabellenHttpAdapter sut;

	MockMvc mockMvc;

	@BeforeEach
	void setup() {
		mockMvc = standaloneSetup(sut).build();
	}

	@MockBean
	TabellenService tabellenService;

	@Test
	void approveWithRunningGames() throws Exception {
		String league = "bl1";
		String season = "2023-games-running";
		sut = new TabellenHttpAdapter(new DefaultTabellenService(spieltagFsRepo()));
		mockMvc = standaloneSetup(sut).build();
		String jsonResponse = mockMvc.perform(get("/tabelle/" + league + "/" + season)) //
				.andDo(print()) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();
		verifyJson(jsonResponse);
	}

}
