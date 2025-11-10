package com.chessgame.observer;

import com.chessgame.model.GameState;
import com.chessgame.model.PlayerColor;
import java.util.Objects;

/**
 * Event representing the end of a game
 */
public class GameEndEvent extends GameEvent {
    private final GameState gameState;
    private final PlayerColor winner;
    private final String reason;
    private final int moveCount;
    private final long gameDuration; // in milliseconds
    
    public GameEndEvent(String eventId, GameState gameState, PlayerColor winner, String reason) {
        super(GameEventType.GAME_ENDED, eventId);
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
        this.winner = winner; // Can be null for draws
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.moveCount = 0;
        this.gameDuration = 0;
    }
    
    public GameEndEvent(String eventId, GameState gameState, PlayerColor winner, String reason, 
                       int moveCount, long gameDuration) {
        super(GameEventType.GAME_ENDED, eventId);
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
        this.winner = winner; // Can be null for draws
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.moveCount = moveCount;
        this.gameDuration = gameDuration;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public PlayerColor getWinner() {
        return winner;
    }
    
    public String getReason() {
        return reason;
    }
    
    public int getMoveCount() {
        return moveCount;
    }
    
    public long getGameDuration() {
        return gameDuration;
    }
    
    public long getGameDurationInMinutes() {
        return gameDuration / (1000 * 60);
    }
    
    public boolean isDraw() {
        return winner == null;
    }
    
    public boolean isWhiteWin() {
        return winner == PlayerColor.WHITE;
    }
    
    public boolean isBlackWin() {
        return winner == PlayerColor.BLACK;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Game ended: ").append(reason);
        
        if (winner != null) {
            sb.append(" Winner: ").append(winner);
        } else {
            sb.append(" Result: Draw");
        }
        
        if (moveCount > 0) {
            sb.append(" Moves: ").append(moveCount);
        }
        
        if (gameDuration > 0) {
            sb.append(" Duration: ").append(getGameDurationInMinutes()).append(" minutes");
        }
        
        return sb.toString();
    }
}