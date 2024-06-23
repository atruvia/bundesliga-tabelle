package de.atruvia.ase.samman.buli.util;

import static de.atruvia.ase.samman.buli.util.Merger.enforceUnique;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

}
