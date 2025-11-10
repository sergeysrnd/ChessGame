package com.chessgame.observer;

import com.chessgame.model.PlayerColor;
import com.chessgame.model.Position;
import java.util.Objects;

/**
 * Event representing a check or checkmate situation
 */
public class CheckEvent extends GameEvent {
    private final PlayerColor playerInCheck;
    private final Position kingPosition;
    private final boolean isCheckmate;
    private final Position checkingPiecePosition;
    
    public CheckEvent(String eventId, PlayerColor playerInCheck, Position kingPosition, boolean isCheckmate) {
        super(isCheckmate ? GameEventType.CHECKMATE_DETECTED : GameEventType.CHECK_DETECTED, eventId);
        this.playerInCheck = Objects.requireNonNull(playerInCheck, "Player cannot be null");
        this.kingPosition = Objects.requireNonNull(kingPosition, "King position cannot be null");
        this.isCheckmate = isCheckmate;
        this.checkingPiecePosition = null;
    }
    
    public CheckEvent(String eventId, PlayerColor playerInCheck, Position kingPosition, 
                     Position checkingPiecePosition, boolean isCheckmate) {
        super(isCheckmate ? GameEventType.CHECKMATE_DETECTED : GameEventType.CHECK_DETECTED, eventId);
        this.playerInCheck = Objects.requireNonNull(playerInCheck, "Player cannot be null");
        this.kingPosition = Objects.requireNonNull(kingPosition, "King position cannot be null");
        this.isCheckmate = isCheckmate;
        this.checkingPiecePosition = Objects.requireNonNull(checkingPiecePosition, "Checking piece position cannot be null");
    }
    
    public PlayerColor getPlayerInCheck() {
        return playerInCheck;
    }
    
    public Position getKingPosition() {
        return kingPosition;
    }
    
    public boolean isCheckmate() {
        return isCheckmate;
    }
    
    public Position getCheckingPiecePosition() {
        return checkingPiecePosition;
    }
    
    @Override
    public String toString() {
        if (isCheckmate) {
            return "Checkmate! " + playerInCheck + " king at " + kingPosition + " is checkmated" +
                   (checkingPiecePosition != null ? " by piece at " + checkingPiecePosition : "");
        } else {
            return "Check! " + playerInCheck + " king at " + kingPosition + " is in check" +
                   (checkingPiecePosition != null ? " by piece at " + checkingPiecePosition : "");
        }
    }
}