package sdp.strategy;

import sdp.exception.NoSuitableRouteException;
import sdp.model.DeliveryPriority;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;

import java.util.ArrayList;
import java.util.List;

public class CheapestRouteStrategy implements RouteStrategy {

    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }

    public double calculateTotalCost(Route route, DeliveryRequest request) {
        double distanceFee = route.distanceKm() * 1.2;
        double prioritySurcharge = request.priority() == DeliveryPriority.URGENT ? 25.0 : 0.0;
        return route.baseCost() + distanceFee + prioritySurcharge;
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> validRoutes = new ArrayList<>();
        List<String> rejectedLog = new ArrayList<>();

        filterAndValidate(routes, request, validRoutes, rejectedLog);

        Route bestRoute = null;
        double minTotalCost = Double.MAX_VALUE;

        for (Route route : validRoutes) {
            double totalCost = calculateTotalCost(route, request);

            if (totalCost > request.maxBudget()) {
                rejectedLog.add(String.format("Route %s REJECTED: Total calculated cost (%.2f) exceeds max budget (%.2f).",
                        route.id(), totalCost, request.maxBudget()));
                continue;
            }

            if (totalCost < minTotalCost) {
                minTotalCost = totalCost;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("CheapestRouteStrategy: No route satisfies budget constraints. Rejections: " + rejectedLog);
        }

        String explanation = String.format("Selected %s with minimum total cost $%.2f [Formula: Base($%.2f) + DistanceFee($%.2f) + Surcharge($%.2f)]",
                bestRoute.id(), minTotalCost, bestRoute.baseCost(), bestRoute.distanceKm() * 1.2, request.priority() == DeliveryPriority.URGENT ? 25.0 : 0.0);

        return new RouteDecision(bestRoute, getStrategyName(), minTotalCost, explanation, rejectedLog);
    }
}