package flashcard;

import java.util.List;

/** Defines how flashcards are ordered before each round. */
public interface CardOrganizer {
    List<FlashCard> organize(List<FlashCard> cards);
}
