package sdp.selector;

import sdp.model.DeliveryPriority;
import sdp.model.DeliveryRequest;
import sdp.strategy.*;

import java.util.EnumMap;
import java.util.Map;

public class StrategySelector {

    private final Map<DeliveryPriority, RouteStrategy> strategyRegistry = new EnumMap<>(DeliveryPriority.class);

    public StrategySelector() {
        strategyRegistry.put(DeliveryPriority.URGENT, new FastestRouteStrategy());
        strategyRegistry.put(DeliveryPriority.ECONOMY, new CheapestRouteStrategy());
        strategyRegistry.put(DeliveryPriority.ECO, new EcoFriendlyRouteStrategy());
        strategyRegistry.put(DeliveryPriority.STANDARD, new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20));
        strategyRegistry.put(DeliveryPriority.PREMIUM, new PremiumRouteStrategy());
    }

    public void registerStrategy(DeliveryPriority priority, RouteStrategy strategy) {
        strategyRegistry.put(priority, strategy);
    }

    public RouteStrategy resolveStrategy(DeliveryRequest request) {
        if (request.ecoPreferred()) {
            return strategyRegistry.getOrDefault(DeliveryPriority.ECO, new EcoFriendlyRouteStrategy());
        }

        RouteStrategy matchedStrategy = strategyRegistry.get(request.priority());
        if (matchedStrategy == null) {
            return new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20);
        }
        return matchedStrategy;
    }
}