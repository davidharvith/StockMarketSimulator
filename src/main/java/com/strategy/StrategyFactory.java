package com.strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyFactory {
    private static final List<String> allPossibleStrategies = new ArrayList<>();

    static {
        // Register available strategies
        allPossibleStrategies.add("Buy and Hold");
        allPossibleStrategies.add("Random");
        allPossibleStrategies.add("Moving Average");
        allPossibleStrategies.add("RSI");
        allPossibleStrategies.add("Mean Reversion");
    }

    public static String getAvailableStrategiesNames() {
        return "[" + String.join(", ", allPossibleStrategies) + "]";
    }

    public static Strategy create(String strategyName) {
        switch (strategyName) {
            case "Buy and Hold":
                return new BuyAndHoldEvenly();
            case "Random":
                return new RandomStrategy();
            case "Moving Average":
                return new MovingAverageStrategy();
            case "RSI":
                return new RSIStrategy();
            case "Mean Reversion":
                return new MeanReversionStrategy();
            default:
                return null;
        }
    }
}
