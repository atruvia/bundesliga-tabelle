package de.atruvia.ase.samman.buli.infra.internal;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RestClient {

	private final RestTemplate restTemplate;

	@NonNull
	public <T> T get(String url, Class<T> type, Object... urlVariables) {
		T responseBody = restTemplate.getForObject(url, type, urlVariables);
		if (responseBody == null) {
			throw new RestClientException("No response body available. url=" + url);
		}
		return responseBody;
	}

}
