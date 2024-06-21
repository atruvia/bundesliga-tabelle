package de.atruvia.ase.samman.buli.util;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.empty;
import static lombok.AccessLevel.PRIVATE;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Streams {

	public static <T> BinaryOperator<T> toOnlyElement() {
		return (f, s) -> {
			throw new IllegalStateException("Expected at most one element but found at least " + f + " and " + s);
		};
	}

	@SafeVarargs
	public static <T> Stream<? extends T> concat(Stream<? extends T>... streams) {
		return stream(streams).reduce(Stream::concat).orElse(empty());
	}

}
