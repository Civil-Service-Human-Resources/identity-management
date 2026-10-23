package uk.gov.cshr.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaginationTest {

    @Test
    public void testSmallPageAmountWithoutGaps() {
        Map<String, Integer> result = Pagination.generateList(2, 5);

        assertNotNull(result);
        assertEquals(5, result.size(), "Should contain all 5 pages");
        assertTrue(result.containsKey("1"));
        assertTrue(result.containsKey("5"));
        assertEquals(0, result.get("1"));
        assertEquals(4, result.get("5"));
    }

    @Test
    public void testFirstPageWithGaps() {
        Map<String, Integer> result = Pagination.generateList(1, 30);

        assertNotNull(result);
        assertTrue(result.containsKey("1"), "Should always contain the first page");
        assertTrue(result.containsKey("30"), "Should always contain the last page");
        assertTrue(result.containsKey("..."), "Should contain dots for a large gap");
    }

    @Test
    public void testLastPageWithGaps() {
        Map<String, Integer> result = Pagination.generateList(30, 30);

        assertNotNull(result);
        assertTrue(result.containsKey("1"), "Should always contain the first page");
        assertTrue(result.containsKey("30"), "Should always contain the last page");
        assertTrue(result.containsKey("..."), "Should contain dots for a large gap");
    }

    @Test
    public void testMiddlePageWithGaps() {
        Map<String, Integer> result = Pagination.generateList(15, 50);

        assertNotNull(result);
        assertTrue(result.containsKey("1"), "Should always include page 1");
        assertTrue(result.containsKey("15"), "Should include current page");
        assertTrue(result.containsKey("50"), "Should always include last page");
        assertTrue(result.containsKey("..."), "Should contain gap ellipses");
    }

    @Test
    public void testSinglePage() {
        Map<String, Integer> result = Pagination.generateList(1, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("1"));
        assertEquals(0, result.get("1"));
    }

}
