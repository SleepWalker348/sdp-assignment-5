package sdp;

import sdp.context.RoutePlanner;
import sdp.model.*;
import sdp.selector.StrategySelector;
import sdp.strategy.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("  SMART DELIVERY & ROUTE OPTIMIZATION ENGINE DEMO  ");
        System.out.println("=========================================================\n");

        List<Route> candidateRoutes = createSampleRoutes();
        StrategySelector selector = new StrategySelector();


        System.out.println("--- DEMO 1: RUNTIME STRATEGY SWITCHING ---");
        DeliveryRequest standardReq = new DeliveryRequest("REQ-101", DeliveryPriority.STANDARD, 500.0, 120, 20.0, false, false);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        System.out.println("[Switch 1] Strategy: " + planner.getStrategy().getStrategyName());
        System.out.println(planner.plan(candidateRoutes, standardReq));

        planner.setStrategy(new CheapestRouteStrategy());
        System.out.println("[Switch 2] Strategy: " + planner.getStrategy().getStrategyName());
        System.out.println(planner.plan(candidateRoutes, standardReq));

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        System.out.println("[Switch 3] Strategy: " + planner.getStrategy().getStrategyName());
        System.out.println(planner.plan(candidateRoutes, standardReq));


        System.out.println("--- DEMO 2: AUTOMATIC STRATEGY SELECTION ---");
        DeliveryRequest urgentReq = new DeliveryRequest("REQ-102", DeliveryPriority.URGENT, 1000.0, 45, 30.0, false, false);
        RouteStrategy resolvedStrategy = selector.resolveStrategy(urgentReq);
        planner.setStrategy(resolvedStrategy);
        System.out.println("Auto-Resolved Strategy for URGENT Request: " + resolvedStrategy.getStrategyName());
        System.out.println(planner.plan(candidateRoutes, urgentReq));


        System.out.println("--- DEMO 3: STRATEGY COMPARISON MODE ---");
        runComparisonMode(candidateRoutes, standardReq);
    }

    private static void runComparisonMode(List<Route> routes, DeliveryRequest request) {
        List<RouteStrategy> strategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20),
                new PremiumRouteStrategy()
        );

        System.out.println("=== COMPARISON REPORT FOR " + request.requestId() + " ===");
        for (RouteStrategy st : strategies) {
            try {
                RoutePlanner planner = new RoutePlanner(st);
                RouteDecision decision = planner.plan(routes, request);
                System.out.printf("[%s] -> Selected Route: %s | Score/Metric: %.2f | Cost: $%.2f | Time: %d mins\n",
                        st.getStrategyName(),
                        decision.selectedRoute().id(),
                        decision.calculatedScore(),
                        decision.selectedRoute().baseCost(),
                        decision.selectedRoute().estimatedMinutes());
            } catch (Exception e) {
                System.out.printf("[%s] -> Failed: %s\n", st.getStrategyName(), e.getMessage());
            }
        }
        System.out.println("=========================================================\n");
    }

    private static List<Route> createSampleRoutes() {
        Location locA = new Location("Central Hub", 51.169392, 71.449074);
        Location locB = new Location("Airport Cargo", 51.025281, 71.467182);

        return List.of(
                new Route("ROUTE-A1", locA, locB, 15.0, 25, 45.0, 2.1, 1.0, true, WeatherCondition.CLEAR),
                new Route("ROUTE-B2", locA, locB, 22.0, 18, 80.0, 4.5, 1.1, true, WeatherCondition.CLEAR),
                new Route("ROUTE-C3", locA, locB, 12.0, 35, 25.0, 1.2, 1.4, true, WeatherCondition.RAIN),
                new Route("ROUTE-D4", locA, locB, 18.0, 22, 50.0, 1.8, 1.0, true, WeatherCondition.CLEAR),
                new Route("ROUTE-E5", locA, locB, 30.0, 40, 15.0, 3.0, 1.0, false, WeatherCondition.CLEAR),
                new Route("ROUTE-F6", locA, locB, 10.0, 15, 120.0, 5.0, 1.0, true, WeatherCondition.STORM),
                new Route("ROUTE-G7", locA, locB, 14.0, 28, 35.0, 1.5, 1.2, true, WeatherCondition.SNOW)
        );
    }
}