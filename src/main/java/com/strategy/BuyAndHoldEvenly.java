package com.strategy;

import com.model.Stock;
import com.model.Trader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyAndHoldEvenly implements Strategy {
    private boolean hasBought = false;

    @Override
    public Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader) {
        Map<String, Double> decisionMap = new HashMap<>();

        if (!hasBought && !marketStocks.isEmpty()) {
            double capital = trader.getCash();
            int numStocks = marketStocks.size();
            double allocationPerStock = capital / numStocks; // Evenly distribute capital

            for (Stock stock : marketStocks) {
                decisionMap.put(stock.getSymbol(), allocationPerStock); // Buy quantity

                }
            }

            hasBought = true;


        return decisionMap;
    }

    @Override
    public String getName() {
        return "Buy and Hold";
    }
}
