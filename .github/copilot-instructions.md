# Chess Game - AI Coding Agent Instructions

## Project Overview
A Java Swing-based chess game with full standard chess rules implementation, GUI rendering, and move validation. The architecture separates concerns into game logic (`GamePanel`), piece behavior (`Piece` hierarchy), board rendering (`Board`), and input handling (`Mouse`).

## Architecture & Key Components

### Core Game Loop (`GamePanel.java`)
- **60 FPS game loop** in `run()` method using nanosecond-precision timing
- **Two piece lists**: `pieces` (actual state) and `simPieces` (simulation copy for move validation)
- **State machine**: Manages `promotion`, `gameOver`, `stalemate`, `canMove`, `validSquare` flags
- **Scaling system**: Renders at logical coordinates (850x600 minimum) with `AffineTransform` scaling to fit window size
- **Key pattern**: Always copy `pieces → simPieces` before simulating moves, then copy back if invalid

### Piece System (`src/piece/`)
- **Base class `Piece`**: Defines coordinate system (col/row grid, x/y pixels), image loading, and helper methods
- **Subclasses** (Pawn, Rook, Knight, Bishop, Queen, King): Override `canMove(targetCol, targetRow)` with piece-specific rules
- **Movement validation**: Uses `pieceIsOnStraightLine()` and `pieceIsOnDiagonalLine()` to detect blocking pieces
- **Capture detection**: `getHittingP()` finds target piece; `isValidSquare()` checks if capture is legal
- **Special moves**: Pawn handles en passant (checks `twoStepped` flag), King handles castling via `castlingP` reference

### Board & Rendering (`Board.java`)
- **8x8 grid** with `SQUARE_SIZE=75` pixels, `MARGIN=30` for coordinates
- **Coordinate labels**: Files (a-h) at bottom, ranks (8-1) at left
- **Color scheme**: Light brown (#F0D9B5) and dark brown (#B58863) alternating squares

### Input Handling (`Mouse.java`)
- Simple `MouseAdapter` tracking `x`, `y`, and `pressed` state
- **Coordinate scaling**: `GamePanel.update()` scales mouse coordinates by render scale factor

### Move History (`MoveHistory.java`)
- Stores moves in algebraic notation (e.g., "e4", "Nf3", "exd5")
- Formats output as numbered pairs: "1. e4 c5 2. Nf3..."
- Used for game protocol display in right panel

## Critical Developer Workflows

### Building & Running
```bash
# Compile from workspace root
javac -d bin -sourcepath src src/main/*.java src/piece/*.java

# Run
java -cp bin main.Main
```
Or use `run.bat` script in workspace root.

### Testing Moves
- **Promotion test**: Uncomment `testPromotion()` in `GamePanel.setPieces()` to place pawns near promotion rank
- **Illegal move test**: Uncomment `testIllegal()` to test check/checkmate detection
- **Image loading**: Piece images load from `res/piece/` directory; fallback paths in `Piece.getImage()`

## Project-Specific Patterns

### Move Validation Pipeline
1. **User clicks piece** → `activeP` set if correct color
2. **User drags** → `simulate()` called each frame:
   - Copy `pieces → simPieces`
   - Update `activeP` position to mouse coords
   - Call `activeP.canMove()` (piece-specific rules)
   - Check `isIllegal()` (king in check after move)
   - Check `opponentCanCaptureKing()` (king safety)
   - Set `validSquare = true` only if all checks pass
3. **User releases** → If `validSquare`, commit move; else reset

### Check/Checkmate Detection
- **Check**: `isKingInCheck()` tests if opponent's piece can capture king
- **Checkmate**: `isCheckMate()` verifies king has no legal moves AND cannot block/capture checking piece
- **Blocking logic**: Iterates between checking piece and king, tests if any piece can move to blocking square
- **Stalemate**: `isStalemate()` returns true if only king remains for opponent AND king cannot move

### Coordinate System
- **Grid coords**: `col` (0-7 left-to-right), `row` (0-7 top-to-bottom)
- **Pixel coords**: `x = col * SQUARE_SIZE + MARGIN`, `y = row * SQUARE_SIZE + MARGIN`
- **Conversion**: `getCol(x)` and `getRow(y)` use `HALF_SQUARE_SIZE` for rounding

### Color Constants
- `GamePanel.WHITE = 0`, `GamePanel.BLACK = 1`
- Used in piece constructors and turn logic
- `currentColor` tracks whose turn it is

## Integration Points & Dependencies

### Image Loading
- Pieces load PNG images from `res/piece/` (e.g., "w-pawn.png", "b-rook.png")
- Fallback paths: `bin/res/piece/`, `src/resources/`
- **Critical**: If images missing, pieces render as null (no error thrown)

### Window Resizing
- `ComponentAdapter` listener triggers `repaint()` on resize
- Scaling preserves aspect ratio: `scale = min(scaleX, scaleY)`
- Logical coordinates always 850x600; physical size adjusts

### Turn Management
- `changePlayer()` swaps `currentColor`, resets `twoStepped` flags for all pieces
- Called after valid move (unless promotion active)
- Promotion mode intercepts turn change until piece selected

## Common Pitfalls & Conventions

- **Always use `simPieces` for move validation**, never modify `pieces` directly during simulation
- **Reset `activeP = null`** after move completion or invalid move
- **Check `isWithThinBoard()` first** in piece `canMove()` to prevent out-of-bounds errors
- **Pawn direction**: WHITE moves up (row decreases), BLACK moves down (row increases)
- **En passant**: Only valid if target pawn has `twoStepped == true` from previous move
- **Castling**: Requires `castlingP` reference; rook position updated in `checkCastling()`
- **Promotion**: Blocks normal turn change; must select piece from `promoPieces` list

## AI System (`src/ai/`)

### ChessAI Engine
- **Minimax algorithm** with alpha-beta pruning for move evaluation
- **Difficulty levels** 1-5: adjusts search depth and move randomness
- **Piece valuation**: Pawn=100, Knight=320, Bishop=330, Rook=500, Queen=900, King=20000
- **Move generation**: Validates all legal moves including check/checkmate detection
- **Threading**: AI moves execute in background thread to prevent UI blocking

### Game Mode Selection (`GameModeSelector.java`)
- **Dialog UI** for choosing between PvP and PvAI modes
- **Difficulty slider** (1-5) for AI opponent strength
- Integrates with `Main.java` to pass mode/difficulty to `GamePanel`

### Integration in GamePanel
- **gameMode**: 0 = PvP, 1 = PvAI
- **aiColor**: Always BLACK (player is WHITE)
- **aiThinking**: Flag to prevent multiple simultaneous AI moves
- **makeAIMove()**: Spawns background thread, adds 500-1500ms delay for realism
- **executeMove()**: Unified method for both player and AI moves

## File Structure Reference
```
src/main/
  Main.java              - Entry point, JFrame setup, mode selection
  GamePanel.java         - Game loop, move validation, rendering, AI integration
  Board.java             - Board rendering with coordinates
  Mouse.java             - Input tracking
  MoveHistory.java       - Move notation storage
  GameModeSelector.java  - Game mode and difficulty selection dialog
  Type.java              - Piece type enum

src/piece/
  Piece.java             - Base class, coordinate system, helpers
  Pawn.java              - Pawn-specific movement (en passant, promotion)
  Rook.java              - Straight-line movement
  Knight.java            - L-shaped movement
  Bishop.java            - Diagonal movement
  Queen.java             - Rook + Bishop movement
  King.java              - One-square movement + castling

src/ai/
  ChessAI.java           - Minimax engine with alpha-beta pruning
```

## AI Development Notes

### Adding New Difficulty Levels
Modify `ChessAI` constructor to adjust `maxDepth` based on difficulty:
```java
this.maxDepth = difficulty;  // Currently 1-5, can extend to 6-8
```

### Improving AI Strength
- Increase search depth (currently limited to 5 for performance)
- Add position evaluation heuristics (pawn advancement, king safety, piece centralization)
- Implement opening book for first 10 moves
- Add endgame tablebase lookups

### Debugging AI Moves
- Check `ChessAI.generateAllLegalMoves()` to verify move generation
- Verify `isKingInCheck()` correctly detects check situations
- Test `minimax()` scoring with known positions
