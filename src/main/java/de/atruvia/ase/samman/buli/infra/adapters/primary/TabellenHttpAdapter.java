package de.atruvia.ase.samman.buli.infra.adapters.primary;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import de.atruvia.ase.samman.buli.domain.ports.primary.TabellenService;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueNotFoundException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequiredArgsConstructor
@PrimaryAdapter
public class TabellenHttpAdapter {

	@PrimaryAdapter
	private enum JsonErgebnis {
		N, U, S;

		private static final Map<Ergebnis, JsonErgebnis> mapping = new EnumMap<>(Map.of( //
				SIEG, JsonErgebnis.S, //
				UNENTSCHIEDEN, JsonErgebnis.U, //
				NIEDERLAGE, JsonErgebnis.N //
		));

		public static List<JsonErgebnis> fromDomain(List<Ergebnis> ergebnisse) {
			return ergebnisse.stream().map(JsonErgebnis::fromDomain).toList();
		}

		public static JsonErgebnis fromDomain(Ergebnis ergebnis) {
			return mapping.get(ergebnis);
		}

	}

	@Value
	@Builder
	@PrimaryAdapter
	private static class JsonLaufendesSpiel {
		@Schema(description = "Mögliche Ausprägungen: 'S' (Sieg), 'U' (Unentschieden), 'N' (Niederlage). "
				+ "Da das Spiel noch nicht beendet ist handelt es sich eigentlich nicht um Sieg bzw. Niederlage sondern um Führung bzw. Rückstand. ", allowableValues = {
						"S", "U", "N" })
		JsonErgebnis ergebnis;
		@Schema(description = "Teamname des gegnerischen Teams")
		String gegner;
		@Schema(description = "Geschossene Tore des Teams")
		int tore;
		@Schema(description = "Geschossene Tore des gegnerischen Teams")
		int toreGegner;

		@PrimaryAdapter
		private static class JsonLaufendesSpielBuilder {

		}
	}

	@Value
	@Builder
	@JsonInclude(NON_NULL)
	@PrimaryAdapter
	private static class JsonTabellenPlatz {

		private static final int TENDENZ_MAX_LENGTH = 5;

		int platz;
		@Schema(description = "URI des Vereinswappens/-logos. Im Normallfall gesetzt, kann aber potentiell null sein. ", nullable = true)
		String wappen;
		String team;
		int spiele;
		int punkte;
		int tore, gegentore, tordifferenz;
		int siege, unentschieden, niederlagen;
		@ArraySchema(schema = @Schema(description = "Ergebnisse der letzten fünf Spiele. "
				+ "Enthält 'S' (Sieg), 'U' (Unentschieden), 'N' (Niederlage). Nur beendete (nicht laufende) Spiele werden berücksichtigt. ", pattern = "[SUN]"), maxItems = TENDENZ_MAX_LENGTH)
		List<JsonErgebnis> tendenz;
		@Schema(description = "Information zum Spiel, falls dieses Team derzeit gegen einen andere Mannschaft in dieser Liga spielt, ansonsten nicht gesetzt. ", nullable = true)
		JsonLaufendesSpiel laufendesSpiel;

		private static JsonTabellenPlatz fromDomain(TabellenPlatz domain) {
			JsonTabellenPlatz jsonTabellenPlatz = builder() //
					.platz(domain.platz()) //
					.wappen(domain.team().wappen() == null ? null : domain.team().wappen().toASCIIString()) //
					.team(domain.team().name()) //
					.spiele(domain.spiele()) //
					.punkte(domain.punkte()) //
					.tore(domain.gesamtTore()) //
					.gegentore(domain.gesamtGegentore()) //
					.tordifferenz(domain.torDifferenz()) //
					.siege(domain.siege()) //
					.unentschieden(domain.unentschieden()) //
					.niederlagen(domain.niederlagen()) //
					.tendenz(JsonErgebnis.fromDomain(domain.tendenz().ergebnisse())) //
					.laufendesSpiel(convertLaufendesSpiel(domain)) //
					.build();
			assert jsonTabellenPlatz.tendenz.size() <= TENDENZ_MAX_LENGTH
					: jsonTabellenPlatz.tendenz + " länger als vereinbart ";
			return jsonTabellenPlatz;
		}

		private static JsonLaufendesSpiel convertLaufendesSpiel(TabellenPlatz domain) {
			var paarung = domain.laufendesSpiel();
			return paarung == null //
					? null //
					: new JsonLaufendesSpiel( //
							JsonErgebnis.fromDomain(paarung.ergebnis()), //
							paarung.gegner().team().name(), //
							paarung.tore(), //
							paarung.gegentore() //
					);
		}

		@PrimaryAdapter
		public static class JsonTabellenPlatzBuilder {

		}
	}

	private final TabellenService tabellenService;

	@GetMapping("/tabelle/{league}/{season}")
	public ResponseEntity<List<JsonTabellenPlatz>> getTabelle(@PathVariable String league,
			@PathVariable String season) {
		try {
			List<JsonTabellenPlatz> tabellenPlaetze = getTabellenPlaetze(league, season);
			return tabellenPlaetze.isEmpty() //
					? notFound().build() //
					: ok(tabellenPlaetze);
		} catch (AvailableLeagueNotFoundException e) {
			return notFound().build();
		}
	}

	private List<JsonTabellenPlatz> getTabellenPlaetze(String league, String season) {
		Tabelle tabelle = tabellenService.erstelleTabelle(league, season);
		return tabelle.entries().stream() //
				.map(JsonTabellenPlatz::fromDomain) //
				.toList();
	}

}
