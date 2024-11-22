package de.atruvia.ase.samman.buli.infra;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class HttpErrorHandlingTest {

	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate httpClient;

	@MockitoBean
	TabellenService tabellenService;

	@ParameterizedTest
	@ValueSource(classes = {
			RuntimeException.class,
			Error.class,
	})
	void uncheckedExceptionsAreMappedTo500(Class<Throwable> exceptionType) throws Exception {
		when(tabellenService.erstelleTabelle(any(), any())).thenThrow(exceptionType);

		var url = "http://localhost:" + serverPort + "/tabelle/bl1/2023";
		ResponseEntity<String> responseEntity = httpClient.getForEntity(url, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		
		String responseBody = responseEntity.getBody();
		assertThat(responseBody).startsWith("{").endsWith("}");
		assertThat(responseBody).contains("\"status\":500");
		assertThat(responseBody).contains("\"error\":\"Internal Server Error\"");
		assertThat(responseBody).contains("\"path\":\"/tabelle/bl1/2023\"");
		assertThat(responseBody).contains("\"timestamp\":");
	}
}
