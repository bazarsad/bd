package flashcard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Places cards with the lowest correct-answer ratio first. */
public class WorstFirstSorter implements CardOrganizer {
    @Override
    public List<FlashCard> organize(List<FlashCard> cards) {
        List<FlashCard> result = new ArrayList<>(cards);
        result.sort(Comparator.comparingDouble(c ->
            c.getTotalAttempts() == 0 ? 0.0
                : (double) c.getCorrectAttempts() / c.getTotalAttempts()));
        return result;
    }
}
