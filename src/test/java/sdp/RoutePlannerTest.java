package sdp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sdp.context.RoutePlanner;
import sdp.exception.InvalidRouteDataException;
import sdp.exception.InvalidStrategyConfigurationException;
import sdp.exception.NoSuitableRouteException;
import sdp.model.*;
import sdp.selector.StrategySelector;
import sdp.strategy.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutePlannerTest {

    private List<Route> routes;
    private Location loc1;
    private Location loc2;

    @BeforeEach
    void setUp() {
        loc1 = new Location("Warehouse A", 10.0, 20.0);
        loc2 = new Location("Client B", 10.5, 20.5);

        routes = List.of(
                new Route("R1", loc1, loc2, 10.0, 20, 50.0, 2.0, 1.0, true, WeatherCondition.CLEAR),
                new Route("R2", loc1, loc2, 25.0, 10, 100.0, 5.0, 1.0, true, WeatherCondition.CLEAR),
                new Route("R3", loc1, loc2, 8.0, 40, 20.0, 1.0, 1.0, true, WeatherCondition.CLEAR),
                new Route("R4", loc1, loc2, 15.0, 25, 40.0, 3.0, 1.0, false, WeatherCondition.CLEAR) // Unavailable
        );
    }

    @Test
    @DisplayName("Test 1: Fastest Strategy selects route with minimum adjusted travel time")
    void testFastestStrategy() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-1", DeliveryPriority.URGENT, 200.0, 60, 10.0, false, false);
        RouteDecision decision = planner.plan(routes, request);

        assertEquals("R2", decision.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 2: Cheapest Strategy selects route with minimum calculated total cost")
    void testCheapestStrategy() {
        RoutePlanner planner = new RoutePlanner(new CheapestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-2", DeliveryPriority.ECONOMY, 200.0, 60, 10.0, false, false);
        RouteDecision decision = planner.plan(routes, request);

        assertEquals("R3", decision.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 3: Eco Strategy selects route with lowest environmental footprint")
    void testEcoStrategy() {
        RoutePlanner planner = new RoutePlanner(new EcoFriendlyRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-3", DeliveryPriority.ECO, 200.0, 60, 10.0, true, false);
        RouteDecision decision = planner.plan(routes, request);

        assertEquals("R3", decision.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 4: Balanced Strategy calculates multi-criteria weighted score")
    void testBalancedStrategy() {
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20));
        DeliveryRequest request = new DeliveryRequest("REQ-4", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);
        RouteDecision decision = planner.plan(routes, request);

        assertNotNull(decision.selectedRoute());
    }

    @Test
    @DisplayName("Test 5: Runtime strategy switching modifies behavior on same planner instance")
    void testRuntimeStrategySwitching() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-5", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);

        RouteDecision decision1 = planner.plan(routes, request);
        assertEquals("R2", decision1.selectedRoute().id());

        planner.setStrategy(new CheapestRouteStrategy());
        RouteDecision decision2 = planner.plan(routes, request);
        assertEquals("R3", decision2.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 6: StrategySelector correctly maps request priority to strategy")
    void testAutomaticStrategySelection() {
        StrategySelector selector = new StrategySelector();
        DeliveryRequest reqUrgent = new DeliveryRequest("REQ-6A", DeliveryPriority.URGENT, 200.0, 60, 10.0, false, false);

        RouteStrategy resolved = selector.resolveStrategy(reqUrgent);
        assertInstanceOf(FastestRouteStrategy.class, resolved);
    }

    @Test
    @DisplayName("Test 7: Hard Constraint filters out unavailable routes")
    void testHardConstraintUnavailableRoute() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-7", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);

        RouteDecision decision = planner.plan(routes, request);
        assertNotEquals("R4", decision.selectedRoute().id(), "Unavailable route R4 must not be chosen");
    }

    @Test
    @DisplayName("Test 8: Hard Constraint filters out routes exceeding budget limit")
    void testBudgetConstraintFiltering() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-8", DeliveryPriority.URGENT, 45.0, 60, 10.0, false, false);

        RouteDecision decision = planner.plan(routes, request);
        assertNotEquals("R2", decision.selectedRoute().id(), "R2 costs $100 and must be rejected under $45 budget");
    }

    @Test
    @DisplayName("Test 9: Deadline restriction constraint rejects overly slow routes")
    void testDeadlineConstraintFiltering() {
        RoutePlanner planner = new RoutePlanner(new CheapestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-9", DeliveryPriority.ECONOMY, 200.0, 15, 10.0, false, false);

        RouteDecision decision = planner.plan(routes, request);
        assertNotEquals("R3", decision.selectedRoute().id(), "R3 takes 40 minutes and exceeds 15-minute deadline");
    }

    @Test
    @DisplayName("Test 10: Throws NoSuitableRouteException when no route satisfies constraints")
    void testNoSuitableRouteException() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest impossibleRequest = new DeliveryRequest("REQ-10", DeliveryPriority.URGENT, 5.0, 2, 0.1, false, false);

        assertThrows(NoSuitableRouteException.class, () -> planner.plan(routes, impossibleRequest));
    }

    @Test
    @DisplayName("Test 11: Invalid Balanced Strategy weights throw InvalidStrategyConfigurationException")
    void testInvalidBalancedWeights() {
        assertThrows(InvalidStrategyConfigurationException.class, () ->
                new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));
    }

    @Test
    @DisplayName("Test 12: New Fifth Strategy (PremiumRouteStrategy) operates as expected")
    void testPremiumStrategy() {
        RoutePlanner planner = new RoutePlanner(new PremiumRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-12", DeliveryPriority.PREMIUM, 200.0, 60, 10.0, false, true);

        RouteDecision decision = planner.plan(routes, request);
        assertNotNull(decision.selectedRoute());
        assertEquals("Premium VIP Route Strategy", decision.strategyName());
    }

    @Test
    @DisplayName("Test 13: Compares results across multiple strategies")
    void testStrategyComparison() {
        DeliveryRequest request = new DeliveryRequest("REQ-13", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);

        RouteDecision fastDecision = new RoutePlanner(new FastestRouteStrategy()).plan(routes, request);
        RouteDecision cheapDecision = new RoutePlanner(new CheapestRouteStrategy()).plan(routes, request);

        assertNotEquals(fastDecision.selectedRoute().id(), cheapDecision.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 14: Boundary case with single available route")
    void testSingleRouteBoundaryCase() {
        List<Route> singleRouteList = List.of(routes.get(0));
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-14", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);

        RouteDecision decision = planner.plan(singleRouteList, request);
        assertEquals("R1", decision.selectedRoute().id());
    }

    @Test
    @DisplayName("Test 15: Failure scenario on passing empty route list throws InvalidRouteDataException")
    void testEmptyRouteListFailure() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = new DeliveryRequest("REQ-15", DeliveryPriority.STANDARD, 200.0, 60, 10.0, false, false);

        assertThrows(InvalidRouteDataException.class, () -> planner.plan(Collections.emptyList(), request));
    }
}