package de.atruvia.ase.samman.buli.util;

import static de.atruvia.ase.samman.buli.util.Merger.checkUnique;
import static de.atruvia.ase.samman.buli.util.Merger.lastNonNull;
import static de.atruvia.ase.samman.buli.util.Merger.merge;
import static de.atruvia.ase.samman.buli.util.Merger.sum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.util.Merger.Mergeable;
import lombok.RequiredArgsConstructor;

class MergerTest {

	@Test
	void testMerge() {
		@RequiredArgsConstructor
		class FooBar implements Mergeable<FooBar> {

			private final int someInt;
			private final List<Integer> someIntValues;
			private final List<String> someStrings;

			@Override
			public FooBar mergeWith(FooBar other) {
				return new FooBar(//
						sum(someInt, other.someInt), //
						merge(someIntValues, other.someIntValues), //
						merge(someStrings, other.someStrings) //
				);
			}
		}
		var merged = Merger.merge( //
				new FooBar(42, List.of(1, 2, 3), List.of("a")), //
				new FooBar(43, List.of(4, 5), List.of("b", "c")) //
		);

		assertThat(merged.someInt).isEqualTo(42 + 43);
		assertThat(merged.someIntValues).isEqualTo(List.of(1, 2, 3, 4, 5));
		assertThat(merged.someStrings).isEqualTo(List.of("a", "b", "c"));
	}

	@Test
	void testCheckUnique_empty() {
		assertThatThrownBy(Merger::checkUnique).hasMessageContaining("empty");
	}

	@Test
	void testCheckUnique_allUnique() {
		assertThat(checkUnique("a", "a", "a")).isEqualTo("a");
	}

	@Test
	void testCheckUnique_notAllUnique() {
		assertThatThrownBy(() -> checkUnique("a", "A", "a")).hasMessageContaining("a, A, a");
	}

	@Test
	void testLastNonNull() {
		assertThat(lastNonNull("a", "b", null)).hasValue("b");
		var lastNonNull = lastNonNull(null, null, null);
		assertThat(lastNonNull).isEmpty();
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
