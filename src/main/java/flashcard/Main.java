package flashcard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point for the Flashcard CLI.
 *
 * <pre>
 * Usage: flashcard &lt;cards-file&gt; [options]
 *   --help                     Show this help and exit
 *   --order &lt;order&gt;            random | worst-first | recent-mistakes-first (default: random)
 *   --repetitions &lt;num&gt;        Correct answers required per card (default: 1)
 *   --invertCards              Swap question and answer (default: false)
 *
 * Cards file format:
 *   # comment
 *   Q: Question text
 *   A: Answer text
 * </pre>
 */
public final class Main {

    private static final double SPEEDY_SECONDS = 5.0;
    private static final int REPEAT_THRESHOLD = 5;
    private static final int CONFIDENT_THRESHOLD = 3;

    private Main() {}

    /** Application entry point. */
    public static void main(String[] args) {
        // --- Parse CLI ---
        boolean help = false;
        String cardsFile = null;
        String order = "random";
        int repetitions = 1;
        boolean invertCards = false;

        for (String a : args) {
            if ("--help".equals(a)) {
                help = true;
                break;
            }
        }
        if (help) {
            printHelp();
            return;
        }
        if (args.length == 0 || args[0].startsWith("--")) {
            System.err.println("Error: expected <cards-file> as first argument.");
            System.err.println("Use --help for usage.");
            System.exit(1);
            return;
        }
        cardsFile = args[0];
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--order":
                    if (i + 1 >= args.length) {
                        die("--order requires a value.");
                    }
                    order = args[++i];
                    if (!order.equals("random") && !order.equals("worst-first")
                        && !order.equals("recent-mistakes-first")) {
                        die("Invalid --order: '" + order
                            + "'. Choose: random, worst-first, recent-mistakes-first");
                    }
                    break;
                case "--repetitions":
                    if (i + 1 >= args.length) {
                        die("--repetitions requires a value.");
                    }
                    try {
                        repetitions = Integer.parseInt(args[++i]);
                        if (repetitions < 1) {
                            die("--repetitions must be >= 1.");
                        }
                    } catch (NumberFormatException e) {
                        die("--repetitions requires an integer, got: '" + args[i] + "'");
                    }
                    break;
                case "--invertCards":
                    invertCards = true;
                    break;
                default:
                    die("Unknown option: '" + args[i] + "'");
            }
        }

        // --- Load cards ---
        List<FlashCard> cards;
        try {
            cards = parseCards(cardsFile);
        } catch (IOException e) {
            System.err.println("Cannot read '" + cardsFile + "': " + e.getMessage());
            System.exit(1);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid file: " + e.getMessage());
            System.exit(1);
            return;
        }

        // --- Build organizer ---
        CardOrganizer organizer;
        switch (order) {
            case "worst-first":
                organizer = new WorstFirstSorter();
                break;
            case "recent-mistakes-first":
                organizer = new RecentMistakesFirstSorter();
                break;
            default:
                organizer = new RandomSorter();
        }

        // --- Run session ---
        runSession(cards, organizer, repetitions, invertCards);
    }

    // -------------------------------------------------------------------------
    // Card file parser — format: Q: / A: lines, # comments, blank lines ignored
    // -------------------------------------------------------------------------
    private static List<FlashCard> parseCards(String filePath) throws IOException {
        List<FlashCard> cards = new ArrayList<>();
        String pending = null;
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("Q:")) {
                    if (pending != null) {
                        throw new IllegalArgumentException(
                            "Line " + lineNum + ": new Q: before A: for: " + pending);
                    }
                    pending = line.substring(2).trim();
                    if (pending.isEmpty()) {
                        throw new IllegalArgumentException("Line " + lineNum + ": empty Q:");
                    }
                } else if (line.startsWith("A:")) {
                    if (pending == null) {
                        throw new IllegalArgumentException(
                            "Line " + lineNum + ": A: without preceding Q:");
                    }
                    String answer = line.substring(2).trim();
                    if (answer.isEmpty()) {
                        throw new IllegalArgumentException("Line " + lineNum + ": empty A:");
                    }
                    cards.add(new FlashCard(pending, answer));
                    pending = null;
                } else {
                    throw new IllegalArgumentException(
                        "Line " + lineNum + ": unrecognized line: '" + line + "'");
                }
            }
        }

        if (pending != null) {
            throw new IllegalArgumentException("File ended with unanswered Q: " + pending);
        }
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("No cards found in: " + filePath);
        }
        return cards;
    }

    // -------------------------------------------------------------------------
    // Session loop
    // -------------------------------------------------------------------------
    private static void runSession(List<FlashCard> cards, CardOrganizer organizer,
            int requiredReps, boolean invertCards) {
        System.out.println("=== Flashcard Session | cards: " + cards.size()
            + " | reps: " + requiredReps
            + (invertCards ? " | inverted" : "") + " ===\n");

        Map<FlashCard, Integer> correctCounts = new HashMap<>();
        for (FlashCard c : cards) {
            correctCounts.put(c, 0);
        }
        List<String> achievements = new ArrayList<>();
        int round = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                List<FlashCard> remaining = new ArrayList<>();
                for (FlashCard c : cards) {
                    if (correctCounts.get(c) < requiredReps) {
                        remaining.add(c);
                    }
                }
                if (remaining.isEmpty()) {
                    break;
                }

                round++;
                for (FlashCard c : remaining) {
                    c.resetRound();
                }

                List<FlashCard> roundCards = organizer.organize(remaining);
                System.out.println("--- Round " + round
                    + " (" + roundCards.size() + " card(s)) ---");

                long start = System.currentTimeMillis();
                boolean allCorrect = true;

                for (FlashCard card : roundCards) {
                    String q = invertCards ? card.getAnswer() : card.getQuestion();
                    String a = invertCards ? card.getQuestion() : card.getAnswer();
                    System.out.print("Q: " + q + "\nYour answer: ");
                    boolean correct = scanner.nextLine().trim().equalsIgnoreCase(a);
                    card.recordAttempt(correct);
                    if (correct) {
                        correctCounts.put(card, correctCounts.get(card) + 1);
                        System.out.println("  ✓ Correct!\n");
                    } else {
                        allCorrect = false;
                        System.out.println("  ✗ Incorrect. Answer: " + a + "\n");
                    }
                }

                double elapsed = (System.currentTimeMillis() - start) / 1000.0;
                checkAchievements(roundCards, elapsed, allCorrect, achievements);
            }
        }

        // Summary
        System.out.println("=== Done in " + round + " round(s) ===");
        for (FlashCard c : cards) {
            System.out.printf("  %-40s %d/%d correct%n",
                c.getQuestion().length() > 40
                    ? c.getQuestion().substring(0, 37) + "..." : c.getQuestion(),
                c.getCorrectAttempts(), c.getTotalAttempts());
        }
        System.out.println(achievements.isEmpty()
            ? "No achievements." : "Achievements: " + achievements);
    }

    // -------------------------------------------------------------------------
    // Achievements
    // -------------------------------------------------------------------------
    private static void checkAchievements(List<FlashCard> cards, double seconds,
            boolean allCorrect, List<String> unlocked) {
        if (!cards.isEmpty() && seconds / cards.size() < SPEEDY_SECONDS) {
            award("SPEEDY", unlocked);
        }
        if (allCorrect && !cards.isEmpty()) {
            award("CORRECT", unlocked);
        }
        for (FlashCard c : cards) {
            if (c.getTotalAttempts() > REPEAT_THRESHOLD) {
                award("REPEAT", unlocked);
            }
            if (c.getCorrectAttempts() >= CONFIDENT_THRESHOLD) {
                award("CONFIDENT", unlocked);
            }
        }
    }

    private static void award(String name, List<String> unlocked) {
        if (!unlocked.contains(name)) {
            unlocked.add(name);
            System.out.println("  ★ Achievement: " + name);
        }
    }

    // -------------------------------------------------------------------------
    // Help & error
    // -------------------------------------------------------------------------
    private static void printHelp() {
        System.out.println("Usage: flashcard <cards-file> [options]\n");
        System.out.println("Options:");
        System.out.println("  --help                   Show this help and exit");
        System.out.println("  --order <order>          random | worst-first |"
            + " recent-mistakes-first  (default: random)");
        System.out.println("  --repetitions <num>      Correct answers required"
            + " per card  (default: 1)");
        System.out.println("  --invertCards            Swap question and answer"
            + "  (default: false)\n");
        System.out.println("Cards file (UTF-8):");
        System.out.println("  # comment");
        System.out.println("  Q: Question");
        System.out.println("  A: Answer\n");
        System.out.println("Achievements: SPEEDY  CORRECT  REPEAT  CONFIDENT");
    }

    private static void die(String msg) {
        System.err.println("Error: " + msg);
        System.err.println("Use --help for usage.");
        System.exit(1);
    }
}
