# Quick Start Guide - Chess Game with AI

## Installation & Running

### Option 1: Using run.bat (Recommended)
```bash
cd ChessGame
run.bat
```

### Option 2: Manual Compilation
```bash
# Compile all Java files
javac -d bin -sourcepath src src\main\*.java src\piece\*.java src\ai\*.java

# Run the game
java -cp bin main.Main
```

## First Game

1. **Game Mode Selection**
   - Choose "Человек vs Человек" (Human vs Human) for PvP
   - Choose "Человек vs Компьютер" (Human vs Computer) for PvAI

2. **If Playing Against AI**
   - Select difficulty level (1-5)
   - Level 1-2: Easy (AI makes random moves)
   - Level 3: Medium (balanced play)
   - Level 4-5: Hard (strong opponent)

3. **Playing the Game**
   - Click on a piece to select it
   - Drag to a valid square (highlighted in white)
   - Release to move
   - Invalid moves are highlighted in gray

4. **Game Status**
   - Top right shows whose turn it is
   - Red text indicates check
   - Move history displayed on the right panel

## Game Rules

### Standard Chess Rules
- ✅ Pawn: Moves forward 1-2 squares, captures diagonally
- ✅ Knight: L-shaped moves (2+1 squares)
- ✅ Bishop: Diagonal moves
- ✅ Rook: Horizontal/vertical moves
- ✅ Queen: Rook + Bishop moves
- ✅ King: One square in any direction

### Special Moves
- ✅ **Castling**: King moves 2 squares toward rook (if neither moved)
- ✅ **En Passant**: Pawn captures opponent's pawn that just moved 2 squares
- ✅ **Promotion**: Pawn reaching opposite end becomes Queen/Rook/Bishop/Knight

### Game End Conditions
- ✅ **Checkmate**: King in check with no legal moves
- ✅ **Stalemate**: King not in check but no legal moves
- ✅ **Check**: King under attack (must move to safety)

## AI Difficulty Levels

| Level | Name | Behavior | Time |
|-------|------|----------|------|
| 1 | Очень легко | Random moves | < 100ms |
| 2 | Легко | Random good moves | < 100ms |
| 3 | Средний | Balanced analysis | 500-1500ms |
| 4 | Сложный | Strong play | 1-3 seconds |
| 5 | Очень сложный | Maximum strength | 3-10 seconds |

## Tips for Playing

### Against Easy AI (Levels 1-2)
- Focus on basic tactics
- Capture undefended pieces
- Control the center

### Against Medium AI (Level 3)
- Avoid hanging pieces
- Look for tactical opportunities
- Develop pieces early

### Against Hard AI (Levels 4-5)
- Play solid, positional chess
- Avoid tactical blunders
- Plan several moves ahead
- Use your opening knowledge

## Keyboard Shortcuts
- None currently (use mouse for all moves)

## Troubleshooting

### Game won't start
```bash
# Check Java is installed
java -version

# Recompile everything
javac -d bin -sourcepath src src\main\*.java src\piece\*.java src\ai\*.java
```

### AI moves are too slow
- Use lower difficulty level (1-3)
- Reduce search depth in ChessAI.java

### AI makes bad moves
- Try higher difficulty level
- Check that AI is not in "thinking" state

### Pieces don't display
- Ensure `res/piece/` directory exists with PNG images
- Check file names: `w-pawn.png`, `b-rook.png`, etc.

## File Structure

```
ChessGame/
├── src/
│   ├── main/
│   │   ├── Main.java              ← Entry point
│   │   ├── GamePanel.java         ← Game logic
│   │   ├── GameModeSelector.java  ← Mode selection
│   │   ├── Board.java
│   │   ├── Mouse.java
│   │   ├── MoveHistory.java
│   │   └── Type.java
│   ├── piece/
│   │   ├── Piece.java
│   │   ├── Pawn.java
│   │   ├── Knight.java
│   │   ├── Bishop.java
│   │   ├── Rook.java
│   │   ├── Queen.java
│   │   └── King.java
│   └── ai/
│       └── ChessAI.java           ← AI engine
├── res/
│   └── piece/                     ← Chess piece images
├── bin/                           ← Compiled classes
├── run.bat                        ← Quick start script
├── README.md                      ← Main documentation
├── AI_GUIDE.md                    ← AI documentation
└── QUICKSTART.md                  ← This file
```

## Documentation

- **README.md** - Project overview and features
- **AI_GUIDE.md** - Detailed AI system documentation
- **IMPLEMENTATION_SUMMARY.md** - What was added and how
- **.github/copilot-instructions.md** - Developer guide for AI agents

## Next Steps

### To Improve the AI
See [AI_GUIDE.md](AI_GUIDE.md) for:
- How to increase search depth
- Adding opening books
- Improving position evaluation
- Performance optimization

### To Modify the Game
See [.github/copilot-instructions.md](.github/copilot-instructions.md) for:
- Architecture overview
- Code patterns and conventions
- Integration points
- Common pitfalls

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review AI_GUIDE.md for AI-specific issues
3. Check .github/copilot-instructions.md for architecture questions

## Have Fun!

Enjoy playing chess against the AI! 🎮♟️

---

**Version**: 1.0 with AI Support  
**Last Updated**: October 30, 2025
