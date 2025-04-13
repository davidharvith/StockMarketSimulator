package com.strategy;

import java.util.*;

/**
 * Factory class for creating trading strategy instances.
 */
public class StrategyFactory {

    // Strategy name constants
    public static final String STRATEGY_BUY_AND_HOLD = "Buy and Hold";
    public static final String STRATEGY_RANDOM = "Random";
    public static final String STRATEGY_MOVING_AVERAGE = "Moving Average";
    public static final String STRATEGY_RSI = "RSI";
    public static final String STRATEGY_MEAN_REVERSION = "Mean Reversion";

    private static final List<String> allPossibleStrategies = Arrays.asList(
            STRATEGY_BUY_AND_HOLD,
            STRATEGY_RANDOM,
            STRATEGY_MOVING_AVERAGE,
            STRATEGY_RSI,
            STRATEGY_MEAN_REVERSION
    );

    /**
     * Returns a string representation of all available strategy names.
     */
    public static String getAvailableStrategiesNames() {
        return "[" + String.join(", ", allPossibleStrategies) + "]";
    }

    /**
     * Creates a strategy instance based on the given strategy name.
     *
     * @param strategyName The name of the strategy to create.
     * @return The corresponding Strategy instance, or null if the name is unrecognized.
     */
    public static Strategy create(String strategyName) {
        switch (strategyName) {
            case STRATEGY_BUY_AND_HOLD:
                return new BuyAndHoldEvenly();
            case STRATEGY_RANDOM:
                return new RandomStrategy();
            case STRATEGY_MOVING_AVERAGE:
                return new MovingAverageStrategy();
            case STRATEGY_RSI:
                return new RSIStrategy();
            case STRATEGY_MEAN_REVERSION:
                return new MeanReversionStrategy();
            default:
                return null;
        }
    }
}
