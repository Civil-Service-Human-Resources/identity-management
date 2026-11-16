package uk.gov.cshr.service;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pagination {
    public static Map<String, Integer> generateList(int currentPage, int pageAmount) {
        int delta = 9;
        int left = currentPage - delta;
        int right = currentPage + delta + 1;

        List<Integer> range = new ArrayList<>();

        for (int i = 1; i <= pageAmount; i++) {
            if (i == 1 || i == pageAmount || (i >= left && i < right)) {
                range.add(i);
            }
        }

        return getStringIntegerMap(range);
    }

    private static @NonNull Map<String, Integer> getStringIntegerMap(List<Integer> range) {
        Map<String, Integer> rangeWithDots = new LinkedHashMap<>();
        int previousPage = 0;

        for (int page : range) {
            if (previousPage > 0) {
                int diff = page - previousPage;
                if (diff == 2) {
                    rangeWithDots.put("", previousPage + 1);
                } else if (diff != 1) {
                    rangeWithDots.put("...", 0);
                }
            }
            previousPage = page;
            rangeWithDots.put(String.valueOf(page), page - 1);
        }
        return rangeWithDots;
    }
}
