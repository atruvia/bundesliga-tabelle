package de.atruvia.ase.samman.buli.util;

import static de.atruvia.ase.samman.buli.util.Merger.enforceUnique;
import static de.atruvia.ase.samman.buli.util.Merger.lastNonNull;
import static de.atruvia.ase.samman.buli.util.Merger.merge;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

class MergerTest {

	@Test
	void testEnforceUnique_allUnique() {
		assertThat(enforceUnique("a", "a", "a")).isEqualTo("a");
	}

	@Test
	void testEnforceUnique_notAllUnique() {
		assertThatThrownBy(() -> enforceUnique("a", "A", "a")).hasMessageContaining("a, A, a");
	}

	@Test
	void testLastNonNull() {
		assertThat(lastNonNull(null, "a", null)).isEqualTo("a");
		var lastNonNull = lastNonNull(null, null);
		assertThat(lastNonNull).isNull();
	}

	@Test
	void testMergeLists() {
		assertThat(merge(List.of("a"), List.of("b", "c"))).containsExactly("a", "b", "c");
	}

	@Test
	void testMergeMaps() {
		var merged = merge(Integer::sum, Map.of("a", 1), Map.of("a", 2, "b", 3), Map.of("b", 4), Map.of("c", 5));
		assertThat(merged).containsExactlyInAnyOrderEntriesOf(Map.of("a", 3, "b", 7, "c", 5));
	}

}
