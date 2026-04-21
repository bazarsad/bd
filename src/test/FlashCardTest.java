package flashcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link FlashCard} statistics tracking. */
class FlashCardTest {

    @Test
    void initialStateIsZero() {
        FlashCard c = new FlashCard("Q", "A");
        assertEquals(0, c.getTotalAttempts());
        assertEquals(0, c.getCorrectAttempts());
        assertFalse(c.isMistakeInCurrentRound());
    }

    @Test
    void correctAttemptCountedProperly() {
        FlashCard c = new FlashCard("Q", "A");
        c.recordAttempt(true);
        assertEquals(1, c.getTotalAttempts());
        assertEquals(1, c.getCorrectAttempts());
        assertFalse(c.isMistakeInCurrentRound());
    }

    @Test
    void incorrectAttemptSetsMistakeFlag() {
        FlashCard c = new FlashCard("Q", "A");
        c.recordAttempt(false);
        assertEquals(1, c.getTotalAttempts());
        assertEquals(0, c.getCorrectAttempts());
        assertTrue(c.isMistakeInCurrentRound());
    }

    @Test
    void resetRoundClearsMistakeFlag() {
        FlashCard c = new FlashCard("Q", "A");
        c.recordAttempt(false);
        c.resetRound();
        assertFalse(c.isMistakeInCurrentRound());
    }
}
