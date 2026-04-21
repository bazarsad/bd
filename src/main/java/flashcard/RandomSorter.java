package flashcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Shuffles cards randomly each round. */
public class RandomSorter implements CardOrganizer {
    @Override
    public List<FlashCard> organize(List<FlashCard> cards) {
        List<FlashCard> result = new ArrayList<>(cards);
        Collections.shuffle(result);
        return result;
    }
}
