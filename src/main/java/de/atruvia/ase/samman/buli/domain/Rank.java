package de.atruvia.ase.samman.buli.domain;

/**
 * Der logische Rank innerhalb der Spielsaison, unabhängig von der Positionierung in der Tabelle
 * (zwei unterschiedliche {@link TabellenPlatz}-Einträge können den gleichen logischen Rank aufweisen).
 */
public record Rank(int value) implements Comparable<Rank> {

    @Override
    public int compareTo(Rank other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
