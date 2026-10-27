package sdp.strategy;

import sdp.exception.NoSuitableRouteException;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;
import sdp.model.WeatherCondition;

import java.util.ArrayList;
import java.util.List;

public class PremiumRouteStrategy implements RouteStrategy {

    @Override
    public String getStrategyName() {
        return "Premium VIP Route Strategy";
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> validRoutes = new ArrayList<>();
        List<String> rejectedLog = new ArrayList<>();

        filterAndValidate(routes, request, validRoutes, rejectedLog);

        Route bestRoute = null;
        double minPenalty = Double.MAX_VALUE;

        for (Route route : validRoutes) {
            if (route.weatherCondition() == WeatherCondition.STORM) {
                rejectedLog.add(String.format("Route %s REJECTED: Weather condition STORM is unsafe for Premium delivery.", route.id()));
                continue;
            }

            double penalty = (route.estimatedMinutes() * 0.6) + (route.trafficMultiplier() * 20);
            if (penalty < minPenalty) {
                minPenalty = penalty;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("PremiumRouteStrategy: No route satisfies VIP safety/speed demands. Rejections: " + rejectedLog);
        }

        String explanation = String.format("Selected VIP route %s with safety penalty rating of %.2f", bestRoute.id(), minPenalty);

        return new RouteDecision(bestRoute, getStrategyName(), minPenalty, explanation, rejectedLog);
    }
}