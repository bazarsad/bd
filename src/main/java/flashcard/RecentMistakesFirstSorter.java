package flashcard;

import java.util.ArrayList;
import java.util.List;

/**
 * Places cards answered incorrectly in the previous round first.
 * Internal order within each group is preserved (stable partition).
 */
public class RecentMistakesFirstSorter implements CardOrganizer {
    @Override
    public List<FlashCard> organize(List<FlashCard> cards) {
        List<FlashCard> mistakes = new ArrayList<>();
        List<FlashCard> others = new ArrayList<>();
        for (FlashCard card : cards) {
            if (card.isMistakeInCurrentRound()) {
                mistakes.add(card);
            } else {
                others.add(card);
            }
        }
        mistakes.addAll(others);
        return mistakes;
    }
}
