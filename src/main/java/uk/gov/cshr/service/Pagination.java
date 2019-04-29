package uk.gov.cshr.service;

import java.util.*;

public class Pagination {
    public static Map<String, Integer> generateList(int currentPage, int pageAmount) {
        int current = currentPage,
                last = pageAmount,
                delta = 9,
                left = current - delta,
                right = current + delta + 1;
        List range = new ArrayList<>();
        Map rangeWithDots = new LinkedHashMap();
        int l = 0;

        for (int i = 1; i <= last; i++) {
            if (i == 1 || i == last || i >= left && i < right) {
                range.add("" + i);
            }
        }

        for (Object i : range) {
            if (l > 0) {
                if (Integer.parseInt(i.toString()) - l == 2) {
                    rangeWithDots.put("", (l + 1));
                } else if (Integer.parseInt(i.toString()) - l != 1) {
                    rangeWithDots.put("...", 0);
                }
            }
            l = Integer.parseInt(i.toString());
            rangeWithDots.put(i, l - 1);
        }

        return rangeWithDots;
    }
}
