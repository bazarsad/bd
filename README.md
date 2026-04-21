# Flashcard CLI

Java flashcard learning system — CLI interface, F.CSA311 Бие даалт №1.

## Build & Run

```bash
mvn package
java -jar target/flashcard.jar sample-cards.txt
java -jar target/flashcard.jar sample-cards.txt --order worst-first --repetitions 3
java -jar target/flashcard.jar --help
```

## Options

| Option | Default | Description |
|---|---|---|
| `--order` | `random` | `random` / `worst-first` / `recent-mistakes-first` |
| `--repetitions <n>` | `1` | Correct answers required per card |
| `--invertCards` | `false` | Swap question and answer |
| `--help` | — | Show help and exit |

## Cards file format

```
# comment
Q: Question text
A: Answer text
```

## Achievements

| Name | Condition |
|---|---|
| SPEEDY | Avg answer < 5s/card in a round |
| CORRECT | All cards correct in a round |
| REPEAT | One card attempted > 5 times |
| CONFIDENT | One card correct ≥ 3 times |

## Structure

```
src/main/java/flashcard/
  Main.java                     # CLI, file parser, session loop, achievements
  FlashCard.java                # Model + statistics
  CardOrganizer.java            # Interface
  RandomSorter.java
  WorstFirstSorter.java
  RecentMistakesFirstSorter.java
src/test/java/flashcard/
  FlashCardTest.java
  CardOrganizerTest.java
```
