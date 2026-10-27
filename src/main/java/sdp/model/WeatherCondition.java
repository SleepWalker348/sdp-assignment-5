package sdp.model;

public enum WeatherCondition {
    CLEAR(1.0),
    RAIN(1.15),
    SNOW(1.30),
    STORM(1.50);

    private final double timeMultiplier;

    WeatherCondition(double timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    public double getTimeMultiplier() {
        return timeMultiplier;
    }
}