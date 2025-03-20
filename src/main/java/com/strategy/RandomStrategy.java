package com.strategy;

import com.model.Stock;
import com.model.Trader;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class RandomStrategy implements Strategy {
    private static final String NAME = "Random";
    private final Random random = new Random();

    @Override
    public Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader) {
        Map<String, Double> decisions = new HashMap<>();

        if (marketStocks.isEmpty()) {
            return decisions; // No stocks to trade
        }

        // Choose a random stock
        Stock stock = marketStocks.get(random.nextInt(marketStocks.size()));
        String symbol = stock.getSymbol();

        // Decide randomly to buy, sell, or do nothing
        int action = random.nextInt(3) - 1; // -1 (sell), 0 (hold), 1 (buy)
        double amountToBuy = 0;
        if (action == 1) { // Buy a random amount within budget
            amountToBuy = random.nextDouble() * trader.getCash();


        } else if (action == -1) { // Sell a random amount of owned stock
            amountToBuy = random.nextDouble()*trader.getPortfolio().getOrDefault(symbol, 0);
        }
        decisions.put(symbol, amountToBuy);
        return decisions;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
