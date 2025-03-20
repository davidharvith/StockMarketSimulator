package com.strategy;

import com.model.Stock;
import com.model.Trader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeanReversionStrategy implements Strategy {

    private static final int MOVING_AVERAGE_DAYS = 50;
    private static final double THRESHOLD = 1.0; // 1 standard deviation threshold
    private static final String NAME = "Mean Reversion";
    @Override
    public Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader) {
        Map<String, Double> decision = new HashMap<>();

        for (Stock stock : marketStocks) {
            double sma = stock.calculateMovingAverage(MOVING_AVERAGE_DAYS);
            double stdDev = stock.calculateStandardDeviation(MOVING_AVERAGE_DAYS);
            double currentPrice = stock.getCurrentPrice();
            String symbol = stock.getSymbol();

            // Sell all holdings if the price is too high (mean + 1 std dev)
            if (currentPrice > sma + THRESHOLD * stdDev) {
                if (trader.getPortfolio().containsKey(symbol)) {
                    decision.put(symbol, -trader.getStockTotalValue(stock)); // Sell all
                }
            }

            // Buy if the price is too low (mean - 1 std dev)
            if (currentPrice < sma - THRESHOLD * stdDev) {
                double amountToInvest = trader.getCash() / marketStocks.size(); // Equal split of cash
                decision.put(symbol, amountToInvest);
            }
        }

        return decision;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
