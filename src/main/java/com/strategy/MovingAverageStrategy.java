package com.strategy;

import com.model.Stock;
import com.model.Trader;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class MovingAverageStrategy implements Strategy {

    private final String NAME = "Moving Average Crossover Strategy";

    @Override
    public Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader) {
        Map<String, Double> decision = new HashMap<>();

        // Sell stocks if the short-term SMA is lower than the long-term SMA
        for (Stock stock : marketStocks) {
            double shortTermSMA = stock.calculateMovingAverage(50);
            double longTermSMA = stock.calculateMovingAverage(200);

            // Sell if the short-term SMA is below the long-term SMA
            if (shortTermSMA < longTermSMA) {
                decision.put(stock.getSymbol(), -trader.getStockTotalValue(stock));
            }
        }

        // After checking sell conditions, decide to buy
        double availableCash = trader.getCash();
        int numStocksToBuy = 0;

        // Count how many stocks we want to buy based on the moving average crossover condition
        for (Stock stock : marketStocks) {
            double shortTermSMA = stock.calculateMovingAverage(50);
            double longTermSMA = stock.calculateMovingAverage(200);

            // If short-term SMA is greater than long-term, decide to buy
            if (shortTermSMA > longTermSMA) {
                numStocksToBuy++;
            }
        }

        // Split the available cash equally for buying the stocks we want to buy
        if (numStocksToBuy > 0) {
            double cashPerStock = availableCash / numStocksToBuy;

            // Allocate the available cash to the stocks we want to buy
            for (Stock stock : marketStocks) {
                double shortTermSMA = stock.calculateMovingAverage(50);
                double longTermSMA = stock.calculateMovingAverage(200);

                if (shortTermSMA > longTermSMA) {
                    decision.put(stock.getSymbol(), cashPerStock);  // Allocate cash for buying this stock
                }
            }
        }

        return decision;
    }

    @Override
    public String getName() {
        return NAME;
    }


}
