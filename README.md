# Chess Game

This is a simple chess game implemented in Java. It allows you to play chess on a graphical user interface.

![image](https://github.com/pnpduy/ChessGame/assets/45529371/add8be29-011e-48ce-8514-3ba91014230a)

## Features

- ✅ Supports standard chess rules (including castling, en passant, promotion)
- ✅ GUI interface for easy gameplay with drag-and-drop moves
- ✅ Pieces move according to their specific rules
- ✅ **AI Opponent** with 5 difficulty levels (Minimax with alpha-beta pruning)
- ✅ Two game modes: Human vs Human (PvP) and Human vs Computer (PvAI)
- ✅ Move history with algebraic notation
- ✅ Check, checkmate, and stalemate detection
- ✅ Responsive UI with window resizing support

## How to Play

### Quick Start
```bash
# Run the game
run.bat

# Or compile and run manually
javac -d bin -sourcepath src src\main\*.java src\piece\*.java src\ai\*.java
java -cp bin main.Main
```

### Game Modes
1. **Human vs Human** - Play against another person on the same computer
2. **Human vs Computer** - Play against AI with 5 difficulty levels:
   - Level 1: Very Easy (random moves)
   - Level 2: Easy (random good moves)
   - Level 3: Medium (Minimax depth 3)
   - Level 4: Hard (Minimax depth 4)
   - Level 5: Very Hard (Minimax depth 5)

## Controls

- **Select piece**: Click on a piece to select it
- **Move piece**: Drag to a valid square and release
- **Valid squares**: Highlighted in white (or gray if illegal due to check)
- **Game mode**: Choose at startup dialog

## AI Features

The AI opponent uses the **Minimax algorithm with alpha-beta pruning** to evaluate positions and select optimal moves. See [AI_GUIDE.md](AI_GUIDE.md) for detailed documentation on:
- How the AI works
- Difficulty levels and performance
- Optimization tips
- How to improve the AI strength

## Credits

- Created by [pnpduy](https://github.com/pnpduy).
- Tutorial by [Ryisnow](https://www.youtube.com/@RyiSnow).
- Chess pieces images by [Ryisnow](https://www.youtube.com/@RyiSnow).
