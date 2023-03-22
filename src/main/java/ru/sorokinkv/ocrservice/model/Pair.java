package ru.sorokinkv.ocrservice.model;

import lombok.Data;

/**
 * Pair class.
 *
 * @param <L> generic.
 * @param <R> generic.
 */
@Data
public class Pair<L, R> {
    L left;
    R right;
}

