package sdp.model;

import java.util.List;

public record RouteDecision(
        Route selectedRoute,
        String strategyName,
        double calculatedScore,
        String explanation,
        List<String> rejectedRoutesLog
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Decision by %s ===\n", strategyName));
        if (selectedRoute != null) {
            sb.append(String.format("Selected Route: %s (%s -> %s)\n", selectedRoute.id(), selectedRoute.origin().name(), selectedRoute.destination().name()));
            sb.append(String.format("Score/Metric: %.2f\n", calculatedScore));
            sb.append(String.format("Reasoning: %s\n", explanation));
        } else {
            sb.append("No route selected.\n");
        }
        sb.append("Evaluated & Rejected Routes:\n");
        for (String log : rejectedRoutesLog) {
            sb.append("  - ").append(log).append("\n");
        }
        return sb.toString();
    }
}