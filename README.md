# Flashcard CLI

F.CSA311 Бие даалт №1.

## Build & Run

```bash
mvn package
java -jar target/flashcard.jar sample-cards.txt 
java -jar target/flashcard.jar sample-cards.txt --order worst-first
java -jar target/flashcard.jar sample-cards.txt --order recent-mistakes-first
java -jar target/flashcard.jar sample-cards.txt --order random
java -jar target/flashcard.jar sample-cards.txt --order worst-first --repetitions 3
java -jar target/flashcard.jar sample-cards.txt --invertCards**
java -jar target/flashcard.jar --help
```

## Options

| Option | Default | Description |
|---|---|---|
| `--order` | `random` | `random` / `worst-first` / `recent-mistakes-first` |
| `--repetitions <n>` | `1` | Correct answers required per card |
| `--invertCards` | `false` | Swap question and answer |
| `--help` | — | Show help and exit |
