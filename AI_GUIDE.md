# Chess Game AI - Полное руководство

## Обзор

Добавлена полнофункциональная система искусственного интеллекта для игры в шахматы против компьютера. AI использует алгоритм **Minimax с альфа-бета отсечением** для оценки позиций и выбора оптимальных ходов.

## Основные возможности

### 1. Два режима игры
- **Человек vs Человек (PvP)** - локальная игра между двумя игроками
- **Человек vs Компьютер (PvAI)** - игра против AI противника

### 2. Пять уровней сложности
1. **Очень легко** - случайные ходы, глубина поиска 1
2. **Легко** - случайные ходы из хороших вариантов, глубина 2
3. **Средний** - minimax с глубиной 3
4. **Сложный** - minimax с глубиной 4
5. **Очень сложный** - minimax с глубиной 5

## Архитектура

### Основные компоненты

#### `GameModeSelector.java`
Диалоговое окно для выбора режима игры и уровня сложности перед началом партии.

```java
// Использование в Main.java
GameModeSelector modeSelector = new GameModeSelector(window);
modeSelector.setVisible(true);
int gameMode = modeSelector.getSelectedMode();      // 0 = PvP, 1 = PvAI
int difficulty = modeSelector.getSelectedDifficulty(); // 1-5
```

#### `ChessAI.java`
Основной класс AI с реализацией алгоритма Minimax.

**Ключевые методы:**
- `findBestMove(pieces, aiColor)` - находит лучший ход для AI
- `minimax(pieces, depth, alpha, beta, isMaximizing, aiColor)` - рекурсивный поиск
- `generateAllLegalMoves(pieces, color)` - генерирует все легальные ходы
- `evaluatePosition(pieces, aiColor)` - оценивает позицию на доске

**Веса фигур:**
```
Пешка:   100
Конь:    320
Слон:    330
Ладья:   500
Ферзь:   900
Король:  20000
```

#### Интеграция в `GamePanel.java`
- `gameMode` - текущий режим (0 = PvP, 1 = PvAI)
- `ai` - экземпляр ChessAI
- `aiColor` - цвет фигур AI (всегда BLACK)
- `aiThinking` - флаг, указывающий, что AI обдумывает ход
- `makeAIMove()` - запускает AI в отдельном потоке
- `executeMove()` - унифицированный метод для выполнения ходов

## Как это работает

### Процесс игры против AI

1. **Инициализация**
   ```
   Main.java → GameModeSelector → GamePanel(gameMode=1, difficulty=3)
   ```

2. **Ход игрока**
   - Игрок выбирает и перемещает фигуру мышью
   - Ход валидируется стандартным механизмом
   - После хода вызывается `changePlayer()` → `currentColor = BLACK`

3. **Ход AI**
   - Обнаруживается, что `currentColor == aiColor && !aiThinking`
   - Вызывается `makeAIMove()` в отдельном потоке
   - AI "думает" 500-1500ms для реалистичности
   - `ChessAI.findBestMove()` анализирует позицию
   - Лучший ход выполняется через `executeMove()`
   - Ход записывается в историю
   - Проверяется шах/мат/пат
   - Ход передается игроку

### Алгоритм Minimax

```
minimax(position, depth, alpha, beta, isMaximizing):
  if depth == 0:
    return evaluatePosition(position)
  
  if isMaximizing (AI ход):
    for each move:
      score = minimax(position_after_move, depth-1, alpha, beta, false)
      alpha = max(alpha, score)
      if beta <= alpha: break  // альфа-бета отсечение
    return alpha
  else (противник ход):
    for each move:
      score = minimax(position_after_move, depth-1, alpha, beta, true)
      beta = min(beta, score)
      if beta <= alpha: break  // альфа-бета отсечение
    return beta
```

## Примеры использования

### Запуск игры
```bash
# Скомпилировать и запустить
run.bat

# Или вручную
javac -d bin -sourcepath src src\main\*.java src\piece\*.java src\ai\*.java
java -cp bin main.Main
```

### Изменение уровня сложности AI
```java
// В GamePanel конструкторе
if (gameMode == 1) {
    this.ai = new ChessAI(difficulty);  // difficulty: 1-5
}

// Или изменить во время игры
ai.setDifficulty(5);  // Максимальная сложность
```

### Добавление новой оценочной функции
```java
// В ChessAI.java, метод evaluatePosition()
private int evaluatePosition(ArrayList<Piece> pieces, int aiColor) {
    int score = 0;
    
    // Базовая оценка материала
    for (Piece piece : pieces) {
        int pieceValue = getPieceValue(piece.type);
        if (piece.color == aiColor) {
            score += pieceValue;
        } else {
            score -= pieceValue;
        }
    }
    
    // Добавить позиционную оценку
    score += evaluatePositionalAdvantage(pieces, aiColor);
    
    return score;
}
```

## Оптимизация и улучшения

### Текущие ограничения
- Максимальная глубина поиска: 5 (для производительности)
- Нет открытой книги дебютов
- Нет таблиц эндшпиля
- Простая оценка позиции (только материал)

### Рекомендуемые улучшения

#### 1. Увеличение глубины поиска
```java
// Для более мощного AI (требует больше времени)
this.maxDepth = 6;  // или 7, 8
```

#### 2. Добавление открытой книги
```java
private Map<String, String> openingBook = new HashMap<>();

public ChessAI(int difficulty) {
    loadOpeningBook();  // Загрузить дебюты
}

private int[] findBestMove(ArrayList<Piece> pieces, int aiColor) {
    String position = boardToFEN(pieces);
    if (openingBook.containsKey(position)) {
        return parseMove(openingBook.get(position));
    }
    // Иначе использовать minimax
}
```

#### 3. Улучшенная оценка позиции
```java
private int evaluatePosition(ArrayList<Piece> pieces, int aiColor) {
    int score = evaluateMaterial(pieces, aiColor);
    score += evaluatePawnStructure(pieces, aiColor);
    score += evaluateKingSafety(pieces, aiColor);
    score += evaluatePieceCentralization(pieces, aiColor);
    return score;
}
```

#### 4. Кэширование позиций (Transposition Table)
```java
private Map<String, Integer> transpositionTable = new HashMap<>();

private int minimax(...) {
    String positionHash = hashPosition(pieces);
    if (transpositionTable.containsKey(positionHash)) {
        return transpositionTable.get(positionHash);
    }
    // ... вычисления ...
    transpositionTable.put(positionHash, score);
    return score;
}
```

## Отладка

### Проверка генерации ходов
```java
// В ChessAI.java
List<Move> moves = generateAllLegalMoves(pieces, aiColor);
System.out.println("Доступные ходы: " + moves.size());
for (Move move : moves) {
    System.out.println(move.fromCol + "," + move.fromRow + 
                      " -> " + move.toCol + "," + move.toRow);
}
```

### Отладка minimax
```java
// Добавить логирование в minimax()
System.out.println("Depth: " + depth + ", Score: " + score + 
                  ", IsMax: " + isMaximizing);
```

### Проверка обнаружения шаха
```java
// Убедиться, что isKingInCheck() работает правильно
boolean inCheck = isKingInCheck(pieces, aiColor);
System.out.println("AI в шахе: " + inCheck);
```

## Производительность

### Время выполнения по уровню сложности
- **Уровень 1-2**: < 100ms (случайные ходы)
- **Уровень 3**: 500-1500ms (глубина 3)
- **Уровень 4**: 1-3 сек (глубина 4)
- **Уровень 5**: 3-10 сек (глубина 5)

### Оптимизация
- Используется альфа-бета отсечение для сокращения количества анализируемых позиций
- AI работает в отдельном потоке, не блокируя UI
- Добавлена задержка 500-1500ms для реалистичности

## Известные проблемы и решения

### Проблема: AI делает странные ходы на низких уровнях
**Решение**: На уровнях 1-2 используются случайные ходы - это нормально.

### Проблема: AI слишком медленно думает
**Решение**: Уменьшите `maxDepth` или оптимизируйте `evaluatePosition()`.

### Проблема: AI не видит мат в несколько ходов
**Решение**: Увеличьте `maxDepth` (требует больше времени на анализ).

## Тестирование

### Тестовые сценарии
1. **Простой мат** - AI должен найти мат в 1 ход
2. **Защита от мата** - AI должен избежать мата в 1 ход
3. **Выигрыш материала** - AI должен захватить незащищенную фигуру
4. **Пат** - AI должен избежать пата, если есть другие ходы

### Запуск тестов
```bash
# Скомпилировать с тестами
javac -d bin -sourcepath src src\main\*.java src\piece\*.java src\ai\*.java

# Запустить игру и проверить AI вручную
java -cp bin main.Main
```

## Заключение

Система AI обеспечивает полнофункциональную игру против компьютера с регулируемой сложностью. Архитектура позволяет легко добавлять улучшения и оптимизации.

Для вопросов и предложений по улучшению AI обратитесь к документации в `.github/copilot-instructions.md`.
