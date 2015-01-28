package findfour.shared.logging;

public enum LogLevel {
    Off(0), Minimal(1), Normal(2), Verbose(3);

    private final int level;

    private LogLevel(int argLevel) {
        this.level = argLevel;
    }

    public int getLevel() {
        return level;
    }
}
