# Assignment 5
# Smart Delivery & Route Optimization Engine (Strategy Pattern)


### 1. What problem does Strategy solve in this project?
It eliminates large conditional branching (`if-else` or `switch` statements) when choosing optimization algorithms. It encapsulates routing algorithms into interchangeable classes that can be dynamically swapped at runtime.

### 2. Where is the Context?
`sdp.context.RoutePlanner` maintains a reference to a `RouteStrategy` object and delegates the execution of algorithm decisions.

### 3. Where is the Strategy interface?
`sdp.strategy.RouteStrategy`.

### 4. What are the Concrete Strategies?
- `FastestRouteStrategy`
- `CheapestRouteStrategy`
- `EcoFriendlyRouteStrategy`
- `BalancedRouteStrategy`
- `PremiumRouteStrategy`

### 5. Where is runtime strategy switching demonstrated?
In `Main.java` inside `demo 1` and in `RoutePlannerTest.java` (`testRuntimeStrategySwitching()`).

### 6. Why is this Strategy rather than simple if/else?
`if/else` hardcodes all algorithms inside a single monolithic class, violating SRP and OCP. Strategy isolates every algorithm into a dedicated class, permitting independent modification and unit testing.

### 7. How can a new Strategy be added?
1. Create a class implementing `RouteStrategy`.
2. Implement `selectRoute(...)`.
3. Pass it to `planner.setStrategy(new MyNewStrategy())`.

### 8. Difference between Strategy and Bridge?
- **Strategy** changes behavioral algorithms dynamically at runtime on an abstraction.
- **Bridge** decouples an abstraction hierarchy from its implementation hierarchy at build/design time.

### 9. Which SOLID principles are demonstrated?
- **SRP**: `RoutePlanner` handles context coordination; concrete strategies perform calculation logic; `StrategySelector` handles strategy lookup.
- **OCP**: You can add new routing strategies without altering `RoutePlanner` or existing strategy classes.
- **DIP**: `RoutePlanner` depends on the `RouteStrategy` abstraction rather than concrete classes.

### 10. Limitations of current architecture
It evaluates candidates linearly ($O(N)$ candidate routes list). Highly complex network routing would require graph-based search algorithms (e.g., Dijkstra/A*).

---

### Architecture Description
The Smart Delivery & Route Optimization Engine is built around the Behavioral Strategy Design Pattern, completely eliminating rigid conditional branching (if-else or switch) when determining the optimal delivery path.

### Core Architectural Layers
1. Context (sdp.context.RoutePlanner):
- Acts as the central coordinator. It maintains a reference to the RouteStrategy abstraction and delegates the routing decision via its plan() method.
- It is completely decoupled from concrete algorithm implementations, adhering to the Dependency Inversion Principle (DIP).
2. Strategy Abstraction (sdp.strategy.RouteStrategy):
- Defines the unified interface contract (selectRoute and getStrategyName) along with a default method handling universal hard constraints (budget limits, route availability, emissions thresholds, and deadlines).
3. Concrete Strategies (sdp.strategy.*):
- Encapsulate individual optimization algorithms (FastestRouteStrategy, CheapestRouteStrategy, EcoFriendlyRouteStrategy, BalancedRouteStrategy, and PremiumRouteStrategy). Each strategy computes its own unique metrics, scores, and rejection explanations independently (Single Responsibility Principle — SRP).
4. Strategy Selector (sdp.selector.StrategySelector):
- Manages automatic strategy resolution. It maps business context parameters (such as DeliveryPriority and user preferences) to the appropriate strategy instance, separating the responsibility of choosing a strategy from executing it.

### Example Console Output
![img_2.png](img_2.png)
### Test Results
![img_1.png](img_1.png)
