# Chess Game - PvP Edition

This is a simple chess game implemented in Java, featuring a clean **Player vs Player (PvP)** experience with a graphical user interface.

![image](https://github.com/pnpduy/ChessGame/assets/45529371/add8be29-011e-48ce-8514-3ba91014230a)

## Recent Updates

**AI Mode Removed** (November 2025) - This version has been refactored to focus solely on local two-player gameplay, removing all AI functionality for a cleaner, more focused gaming experience.

## Features

- ✅ Supports standard chess rules (including castling, en passant, promotion)
- ✅ GUI interface for easy gameplay with drag-and-drop moves
- ✅ Pieces move according to their specific rules
- ✅ **Pure Player vs Player mode** - local two-player chess
- ✅ Move history with algebraic notation
- ✅ Check, checkmate, and stalemate detection
- ✅ Responsive UI with window resizing support
- ✅ Fast startup (no AI loading required)
- ✅ Streamlined interface without mode selection dialogs

## How to Play

### Quick Start
```bash
# Run the game
run.bat

# Or compile and run manually
javac -d bin src\main\*.java src\piece\*.java
java -cp bin main.Main
```

### Game Mode
- **Human vs Human** - Play against another person on the same computer
- Game automatically starts with the standard chess board setup
- No configuration required - jump straight into the action!

## Controls

- **Select piece**: Click on a piece to select it
- **Move piece**: Drag to a valid square and release
- **Valid squares**: Highlighted in white (or gray if illegal due to check)
- **Turn indicator**: Clear visual feedback for whose turn it is

## Technical Improvements

The AI removal has resulted in:
- **Simplified codebase** - Removed 5000+ lines of AI-related code
- **Faster loading** - No AI initialization overhead
- **Better performance** - Reduced memory footprint
- **Cleaner UI** - Streamlined interface without unused options
- **Easier maintenance** - Focused codebase on core chess functionality

## What's Been Removed

- All AI decision-making logic and algorithms
- AI difficulty level selection
- AI-related configuration files
- AI service classes and interfaces
- Legacy AI implementations
- AI model files and compiled classes

## Credits

- Created by [pnpduy](https://github.com/pnpduy)
- Tutorial by [Ryisnow](https://www.youtube.com/@RyiSnow)
- Chess pieces images by [Ryisnow](https://www.youtube.com/@RyiSnow)
- AI removal refactoring by AI-assisted development
