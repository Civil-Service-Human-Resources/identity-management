package uk.gov.cshr.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {

    public static <T> List<List<T>> batchList(List<T> list, Integer batchSize) {
        return IntStream.iterate(0, i -> i + batchSize)
                .limit((int) Math.ceil((double) list.size() / batchSize))
                .mapToObj(i -> list.subList(i, Math.min(i + batchSize, list.size())))
                .collect(Collectors.toList());
    }

}
