package sdp.strategy;

import sdp.exception.NoSuitableRouteException;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;

import java.util.ArrayList;
import java.util.List;

public class EcoFriendlyRouteStrategy implements RouteStrategy {

    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() {
        this(0.7, 0.3);
    }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override
    public String getStrategyName() {
        return "Eco-Friendly Route Strategy";
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> validRoutes = new ArrayList<>();
        List<String> rejectedLog = new ArrayList<>();

        filterAndValidate(routes, request, validRoutes, rejectedLog);

        Route bestRoute = null;
        double lowestEcoScore = Double.MAX_VALUE;

        for (Route route : validRoutes) {
            double ecoScore = (route.carbonEmission() * emissionWeight) + (route.distanceKm() * distanceWeight);

            if (ecoScore < lowestEcoScore) {
                lowestEcoScore = ecoScore;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("EcoFriendlyRouteStrategy: No route satisfies ecological requirements. Rejections: " + rejectedLog);
        }

        String explanation = String.format("Selected %s with eco-score %.2f (Carbon: %.2fkg * %.2f + Dist: %.2fkm * %.2f)",
                bestRoute.id(), lowestEcoScore, bestRoute.carbonEmission(), emissionWeight, bestRoute.distanceKm(), distanceWeight);

        return new RouteDecision(bestRoute, getStrategyName(), lowestEcoScore, explanation, rejectedLog);
    }
}