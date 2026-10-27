package sdp.strategy;

import sdp.exception.InvalidStrategyConfigurationException;
import sdp.exception.NoSuitableRouteException;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;

import java.util.ArrayList;
import java.util.List;

public class BalancedRouteStrategy implements RouteStrategy {

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(double timeWeight, double costWeight, double distanceWeight, double emissionWeight) {
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > 0.0001) {
            throw new InvalidStrategyConfigurationException(
                    String.format("Weights must sum up exactly to 1.0. Current sum: %.4f", sum));
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public String getStrategyName() {
        return "Balanced Route Strategy";
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> validRoutes = new ArrayList<>();
        List<String> rejectedLog = new ArrayList<>();

        filterAndValidate(routes, request, validRoutes, rejectedLog);

        if (validRoutes.isEmpty()) {
            throw new NoSuitableRouteException("BalancedRouteStrategy: No route passed hard constraint filters. Rejections: " + rejectedLog);
        }


        double maxTime = validRoutes.stream().mapToDouble(r -> r.estimatedMinutes() * r.trafficMultiplier()).max().orElse(1.0);
        double maxCost = validRoutes.stream().mapToDouble(Route::baseCost).max().orElse(1.0);
        double maxDist = validRoutes.stream().mapToDouble(Route::distanceKm).max().orElse(1.0);
        double maxEmi = validRoutes.stream().mapToDouble(Route::carbonEmission).max().orElse(1.0);

        Route bestRoute = null;
        double minScore = Double.MAX_VALUE;

        for (Route route : validRoutes) {
            double normTime = (route.estimatedMinutes() * route.trafficMultiplier()) / (maxTime == 0 ? 1 : maxTime);
            double normCost = route.baseCost() / (maxCost == 0 ? 1 : maxCost);
            double normDist = route.distanceKm() / (maxDist == 0 ? 1 : maxDist);
            double normEmi = route.carbonEmission() / (maxEmi == 0 ? 1 : maxEmi);

            double score = (normTime * timeWeight) + (normCost * costWeight) + (normDist * distanceWeight) + (normEmi * emissionWeight);

            if (score < minScore) {
                minScore = score;
                bestRoute = route;
            }
        }


        String explanation = String.format("Selected %s with composite score %.4f (Time W:%.2f, Cost W:%.2f, Dist W:%.2f, Emission W:%.2f)",
                bestRoute.id(), minScore, timeWeight, costWeight, distanceWeight, emissionWeight);

        return new RouteDecision(bestRoute, getStrategyName(), minScore, explanation, rejectedLog);
    }
}