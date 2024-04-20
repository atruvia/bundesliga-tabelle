package de.atruvia.ase.samman.buli.infra.internal;

import static java.util.Comparator.comparing;
import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import lombok.ToString;
import lombok.experimental.FieldDefaults;

public interface OpenLigaDbResultinfoRepo {

	@ToString
	@FieldDefaults(level = PUBLIC)
	public static class Resultinfo {

		@ToString
		@FieldDefaults(level = PUBLIC)
		public static class GlobalResultInfo {
			int id;
		}

		private static Comparator<Resultinfo> byGlobalResultId = comparing(r -> r.globalResultInfo.id);

		int id;
		String name;
		int orderId;
		GlobalResultInfo globalResultInfo;

		public static Resultinfo getEndergebnisType(Collection<Resultinfo> resultinfos) {
			return resultinfos.stream().max(byGlobalResultId)
					.orElseThrow(() -> new IllegalArgumentException("resultinfos is empty"));
		}

	}

	List<Resultinfo> getResultinfos(String league, String season);

}