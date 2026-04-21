package flashcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for all {@link CardOrganizer} implementations. */
class CardOrganizerTest {

    @Test
    void randomSorterReturnsSameCards() {
        FlashCard a = new FlashCard("Q1", "A1");
        FlashCard b = new FlashCard("Q2", "A2");
        List<FlashCard> result = new RandomSorter().organize(Arrays.asList(a, b));
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(a, b)));
    }

    @Test
    void worstFirstPutsLowestRatioFirst() {
        FlashCard worst = new FlashCard("Q1", "A1");
        FlashCard best  = new FlashCard("Q2", "A2");
        FlashCard mid   = new FlashCard("Q3", "A3");
        worst.recordAttempt(false);          // 0/1 = 0.0
        best.recordAttempt(true);            // 1/1 = 1.0
        mid.recordAttempt(true);
        mid.recordAttempt(false);            // 1/2 = 0.5

        List<FlashCard> result = new WorstFirstSorter().organize(
            Arrays.asList(best, mid, worst));

        assertEquals(worst, result.get(0));
        assertEquals(mid,   result.get(1));
        assertEquals(best,  result.get(2));
    }

    @Test
    void recentMistakesFirstMovesMistakesToFront() {
        FlashCard a = new FlashCard("Q1", "A1");
        FlashCard b = new FlashCard("Q2", "A2");
        FlashCard c = new FlashCard("Q3", "A3");
        b.recordAttempt(false); // only b has a mistake

        List<FlashCard> result = new RecentMistakesFirstSorter().organize(
            Arrays.asList(a, b, c));

        assertEquals(b, result.get(0));
    }

    @Test
    void recentMistakesFirstPreservesInternalOrder() {
        FlashCard a = new FlashCard("Q1", "A1");
        FlashCard b = new FlashCard("Q2", "A2");
        FlashCard c = new FlashCard("Q3", "A3");
        a.recordAttempt(false);
        c.recordAttempt(false); // a and c have mistakes, b does not

        List<FlashCard> result = new RecentMistakesFirstSorter().organize(
            Arrays.asList(a, b, c));

        assertEquals(a, result.get(0)); // mistake group: a then c
        assertEquals(c, result.get(1));
        assertEquals(b, result.get(2)); // non-mistake group
    }

    @Test
    void recentMistakesFirstNoMistakesKeepsOrder() {
        FlashCard a = new FlashCard("Q1", "A1");
        FlashCard b = new FlashCard("Q2", "A2");
        assertEquals(Arrays.asList(a, b),
            new RecentMistakesFirstSorter().organize(Arrays.asList(a, b)));
    }
}
