package com.chessgame.observer;

import com.chessgame.model.PlayerColor;
import java.util.Objects;

/**
 * Event representing a timer-related event
 */
public class TimerEvent extends GameEvent {
    private final PlayerColor player;
    private final long remainingTime; // in milliseconds
    private final boolean isExpired;
    
    public TimerEvent(String eventId, PlayerColor player, long remainingTime, boolean isExpired) {
        super(isExpired ? GameEventType.TIMER_EXPIRED : GameEventType.TIMER_STARTED, eventId);
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.remainingTime = remainingTime;
        this.isExpired = isExpired;
    }
    
    public PlayerColor getPlayer() {
        return player;
    }
    
    public long getRemainingTime() {
        return remainingTime;
    }
    
    public boolean isExpired() {
        return isExpired;
    }
    
    public long getRemainingTimeInSeconds() {
        return remainingTime / 1000;
    }
    
    @Override
    public String toString() {
        return "Timer event for " + player + ": " + getRemainingTimeInSeconds() + " seconds remaining" + 
               (isExpired ? " (EXPIRED)" : "");
    }
}