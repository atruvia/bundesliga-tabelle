package de.atruvia.ase.samman.buli.springframework;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.infra.internal.RestClient;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class RestTemplateMock {

	public static RestClient restClient(Function<HttpRequest, String> responseSupplier) {
		return new RestClient(restTemplateMock(responseSupplier));
	}

	public static RestClient restClient(Supplier<MockClientHttpResponse> responseSupplier) {
		return new RestClient(configureMock(new RestTemplate(), responseSupplier));
	}

	public static RestTemplate restTemplateMock(Function<HttpRequest, String> responseSupplier) {
		return configureMock(new RestTemplate(), responseSupplier);
	}

	public static RestTemplate configureMock(RestTemplate restTemplate,
			Function<HttpRequest, String> responseSupplier) {
		return addInterceptor(restTemplate, (req, ___, ____) -> {
			MockClientHttpResponse response = new MockClientHttpResponse(responseSupplier.apply(req).getBytes(), OK);
			response.getHeaders().add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
			return response;
		});
	}

	public static RestTemplate configureMock(RestTemplate restTemplate,
			Supplier<MockClientHttpResponse> responseSupplier) {
		return addInterceptor(restTemplate, (__, ___, ____) -> responseSupplier.get());
	}

	private static RestTemplate addInterceptor(RestTemplate restTemplate, ClientHttpRequestInterceptor interceptor) {
		restTemplate.getInterceptors().add(interceptor);
		return restTemplate;
	}

}
