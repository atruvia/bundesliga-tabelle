package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Tore.tore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Negative;

class ToreTest {

	@Property
	void valid(@ForAll @IntRange(min = 0) int positiveOrZero) {
		var sut = tore(positiveOrZero);
		assertThat(sut.anzahl()).isEqualByComparingTo(positiveOrZero);
	}

	@Property
	void invalidMustContainTheWordNegativeAndTheValue(@ForAll @Negative int negative) {
		assertThatThrownBy(() -> tore(negative)).hasMessageContainingAll("negative", String.valueOf(negative));
	}

}
