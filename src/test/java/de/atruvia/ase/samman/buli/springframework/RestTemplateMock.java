package de.atruvia.ase.samman.buli.springframework;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.function.Function;

import de.atruvia.ase.samman.buli.infra.internal.RestClient;
import org.springframework.http.HttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class RestTemplateMock {

	public static RestClient restClient(Function<HttpRequest, String> responseSupplier) {
		return new RestClient(restTemplateMock(responseSupplier));
	}

	public static RestTemplate restTemplateMock(Function<HttpRequest, String> responseSupplier) {
		return configureMock(new RestTemplate(), responseSupplier);
	}

	public static RestTemplate configureMock(RestTemplate restTemplate,
			Function<HttpRequest, String> responseSupplier) {
		restTemplate.getInterceptors().add((req, body, execution) -> {
			MockClientHttpResponse response = new MockClientHttpResponse(responseSupplier.apply(req).getBytes(), OK);
			response.getHeaders().add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
			return response;
		});
		return restTemplate;
	}

}
