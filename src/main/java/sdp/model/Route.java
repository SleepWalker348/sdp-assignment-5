package sdp.model;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double baseCost,
        double carbonEmission,
        double trafficMultiplier,
        boolean isAvailable,
        WeatherCondition weatherCondition
) {
    public Route {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Route ID cannot be empty");
        if (distanceKm <= 0) throw new IllegalArgumentException("Distance must be positive");
        if (estimatedMinutes <= 0) throw new IllegalArgumentException("Estimated minutes must be positive");
        if (baseCost < 0) throw new IllegalArgumentException("Base cost cannot be negative");
        if (carbonEmission < 0) throw new IllegalArgumentException("Carbon emission cannot be negative");
        if (trafficMultiplier < 1.0) throw new IllegalArgumentException("Traffic multiplier cannot be less than 1.0");
    }
}