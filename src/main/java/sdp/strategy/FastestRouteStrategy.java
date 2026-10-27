package sdp.strategy;

import sdp.exception.NoSuitableRouteException;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;

import java.util.ArrayList;
import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public String getStrategyName() {
        return "Fastest Route Strategy";
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> validRoutes = new ArrayList<>();
        List<String> rejectedLog = new ArrayList<>();

        filterAndValidate(routes, request, validRoutes, rejectedLog);

        Route bestRoute = null;
        double minAdjustedTime = Double.MAX_VALUE;

        for (Route route : validRoutes) {
            double adjustedTime = route.estimatedMinutes()
                    * route.trafficMultiplier()
                    * route.weatherCondition().getTimeMultiplier();

            if (adjustedTime > request.maxDeliveryMinutes()) {
                rejectedLog.add(String.format("Route %s REJECTED: Adjusted time (%.1f min) exceeds deadline limit (%d min).",
                        route.id(), adjustedTime, request.maxDeliveryMinutes()));
                continue;
            }

            if (adjustedTime < minAdjustedTime) {
                minAdjustedTime = adjustedTime;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("FastestRouteStrategy: No route satisfies time and constraints. Rejections: " + rejectedLog);
        }

        String explanation = String.format("Selected %s with lowest adjusted travel time of %.2f mins (Base: %d mins, Traffic: x%.1f, Weather [%s]: x%.2f)",
                bestRoute.id(), minAdjustedTime, bestRoute.estimatedMinutes(), bestRoute.trafficMultiplier(), bestRoute.weatherCondition(), bestRoute.weatherCondition().getTimeMultiplier());

        return new RouteDecision(bestRoute, getStrategyName(), minAdjustedTime, explanation, rejectedLog);
    }
}