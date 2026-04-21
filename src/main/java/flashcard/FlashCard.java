package flashcard;

/** A flashcard with question, answer, and attempt statistics. */
public class FlashCard {

    private final String question;
    private final String answer;
    private int totalAttempts;
    private int correctAttempts;
    private boolean mistakeInCurrentRound;

    public FlashCard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getCorrectAttempts() { return correctAttempts; }
    public boolean isMistakeInCurrentRound() { return mistakeInCurrentRound; }

    /** Records an attempt and updates statistics. */
    public void recordAttempt(boolean correct) {
        totalAttempts++;
        if (correct) {
            correctAttempts++;
        } else {
            mistakeInCurrentRound = true;
        }
    }

    /** Resets the mistake flag at the start of each round. */
    public void resetRound() {
        mistakeInCurrentRound = false;
    }
}
