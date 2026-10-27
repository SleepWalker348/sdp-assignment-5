package sdp.context;

import sdp.exception.InvalidRouteDataException;
import sdp.model.DeliveryRequest;
import sdp.model.Route;
import sdp.model.RouteDecision;
import sdp.strategy.RouteStrategy;

import java.util.List;

public class RoutePlanner {
    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        if (routes == null || routes.isEmpty()) {
            throw new InvalidRouteDataException("Route list cannot be null or empty");
        }
        if (request == null) {
            throw new InvalidRouteDataException("Delivery request cannot be null");
        }
        return strategy.selectRoute(routes, request);
    }
}