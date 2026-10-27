package sdp.model;

public record DeliveryRequest(
        String requestId,
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        double maxCarbonEmissionLimit,
        boolean ecoPreferred,
        boolean requiresGuaranteedSafety
) {
    public DeliveryRequest {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("Request ID cannot be empty");
        if (maxBudget <= 0) throw new IllegalArgumentException("Max budget must be positive");
        if (maxDeliveryMinutes <= 0) throw new IllegalArgumentException("Max delivery minutes must be positive");
    }
}