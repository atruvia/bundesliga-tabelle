package de.atruvia.ase.samman.buli.util;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Merger {

	public interface Mergeable<T> {
		T mergeWith(T other);
	}

	public static <T extends Mergeable<T>> T merge(T first, T second) {
		return first.mergeWith(second);
	}

	public static int merge(int... values) {
		return IntStream.of(values).sum();
	}

	@SafeVarargs
	public static <T> List<T> merge(List<T>... lists) {
		return stream(lists).flatMap(List::stream).toList();
	}

	@SafeVarargs
	public static <K, V> Map<K, V> merge(BinaryOperator<V> mergeFunction, Map<K, V>... maps) {
		return stream(maps).map(Map::entrySet).flatMap(Set::stream)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, HashMap::new));
	}

	@SafeVarargs
	public static <T> T lastNonNull(T... objects) {
		// could be done with streams as well, but then we would consume much more
		// elements than we have to
		// stream(objects).filter(Objects::nonNull).reduce(lastElement()).orElse(null);
		for (var it = asList(objects).listIterator(objects.length); it.hasPrevious();) {
			T prev;
			if ((prev = it.previous()) != null) {
				return prev;
			}
		}
		return null;
	}

	@SafeVarargs
	public static <T> T enforceUnique(T... objects) {
		T ref = objects[0];
		if (Arrays.stream(objects).anyMatch(not(ref::equals))) {
			throw new IllegalStateException("Object differs " + Arrays.toString(objects));
		}
		return ref;
	}

}
