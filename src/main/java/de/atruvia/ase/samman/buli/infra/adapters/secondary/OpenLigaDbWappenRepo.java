package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder;
import de.atruvia.ase.samman.buli.domain.ports.secondary.SpieltagRepo;
import de.atruvia.ase.samman.buli.domain.ports.secondary.WappenRepository;
import lombok.ToString;

@Repository
class OpenLigaDbWappenRepo implements WappenRepository {

	@ToString
	private class TeamInfo {
		String teamName;
		String teamIconUrl;

		URI toDomain() {
			return URI.create(teamIconUrl);
		}

	}

	@Override
	public URI getWappen(String league, String season, String teamName) throws Exception {
		return Arrays.stream(new Gson().fromJson(readJson(league, season), TeamInfo[].class))
				.filter(t -> t.teamName.equals(teamName)).findFirst().map(TeamInfo::toDomain).orElse(null);
	}

	@Override
	public List<URI> lade(String league, String season) throws Exception {
		return Arrays.stream(new Gson().fromJson(readJson(league, season), TeamInfo[].class)).map(TeamInfo::toDomain)
				.collect(toList());
	}

	protected String readJson(String league, String season) throws Exception {
		return HttpClient.newHttpClient()
				.send(HttpRequest.newBuilder(URI.create(makeUrl(league, season))).build(), BodyHandlers.ofString())
				.body();
	}

	private String makeUrl(String league, String season) {
		return "https://api.openligadb.de/getmatchdata/" + league + "/" + season;
	}

}
