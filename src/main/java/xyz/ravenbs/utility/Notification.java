package xyz.ravenbs.utility;

public class Notification {
    public enum Type {
        INFO, WARNING, ERROR, SUCCESS
    }

    private final String title;
    private final String message;
    private final Type type;
    private final long startTime;
    private final long duration;
    
    public Notification(String title, String message, Type type, long durationMs) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = durationMs;
        this.startTime = System.currentTimeMillis();
    }
    
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Type getType() { return type; }
    public long getStartTime() { return startTime; }
    public long getDuration() { return duration; }
    
    public double getProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, Math.min(1, (double) elapsed / duration));
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > duration;
    }
}
