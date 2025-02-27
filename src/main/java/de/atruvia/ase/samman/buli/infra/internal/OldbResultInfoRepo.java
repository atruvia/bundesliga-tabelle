package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.Comparator.comparing;
import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.ToString;
import lombok.experimental.FieldDefaults;

/** Provides read access to results from the Open Liga DB. */
public interface OldbResultInfoRepo {

	@ToString
	@FieldDefaults(level = PUBLIC)
	class OldbResultInfo {

		@ToString
		@FieldDefaults(level = PUBLIC)
		public static class GlobalResultInfo {
			int id;
		}

		private static final Comparator<OldbResultInfo> byGlobalResultId = comparing(r -> r.globalResultInfo.id);

		int id;
		String name;
		int orderId;
		GlobalResultInfo globalResultInfo;

		public static OldbResultInfo endergebnisType(Collection<OldbResultInfo> resultInfos) {
			return tryEndergebnisType(resultInfos)
					.orElseThrow(() -> new IllegalArgumentException("resultInfos is empty"));
		}

		private static Optional<OldbResultInfo> tryEndergebnisType(Collection<OldbResultInfo> resultInfos) {
			return resultInfos.stream().max(byGlobalResultId);
		}

	}

	List<OldbResultInfo> getResultInfos(String league, String season);

}