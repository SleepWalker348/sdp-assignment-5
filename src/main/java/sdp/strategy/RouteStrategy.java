package sdp.strategy;

import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;

import java.util.List;

public interface RouteStrategy {
    RouteDecision selectRoute(List<Route> routes, DeliveryRequest request);
    String getStrategyName();

    default void filterAndValidate(List<Route> routes, DeliveryRequest request, List<Route> validRoutes, List<String> rejectedLog) {
        if (routes == null || routes.isEmpty()) {
            return;
        }

        for (Route route : routes) {
            if (!route.isAvailable()) {
                rejectedLog.add(String.format("Route %s REJECTED: Route is currently marked as unavailable.", route.id()));
                continue;
            }
            if (route.baseCost() > request.maxBudget()) {
                rejectedLog.add(String.format("Route %s REJECTED: Base cost (%.2f) exceeds max budget (%.2f).",
                        route.id(), route.baseCost(), request.maxBudget()));
                continue;
            }
            if (route.carbonEmission() > request.maxCarbonEmissionLimit()) {
                rejectedLog.add(String.format("Route %s REJECTED: Emission (%.2f kg) exceeds maximum limit (%.2f kg).",
                        route.id(), route.carbonEmission(), request.maxCarbonEmissionLimit()));
                continue;
            }

            double adjustedTime = route.estimatedMinutes()
                    * route.trafficMultiplier()
                    * route.weatherCondition().getTimeMultiplier();
            if (adjustedTime > request.maxDeliveryMinutes()) {
                rejectedLog.add(String.format("Route %s REJECTED: Adjusted time (%.1f min) exceeds deadline limit (%d min).",
                        route.id(), adjustedTime, request.maxDeliveryMinutes()));
                continue;
            }
            validRoutes.add(route);
        }
    }
}