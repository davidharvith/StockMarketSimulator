package com.model;

import com.strategy.Strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Trader {
    private final Strategy strategy;        // Strategy the trader follows
    private final String name;              // Name based on strategy
    private double cash;              // Available cash for trading
    private final Map<String, Integer> portfolio; // Maps stock symbols to quantities owned


    // Constructor
    public Trader(Strategy strategy, double initialCash) {
        this.strategy = strategy;
        this.name = strategy.getClass().getSimpleName() + " with "+initialCash;
        this.cash = initialCash;
        this.portfolio = new HashMap<>();
    }

    // Executes a trading decision based on strategy
    public void makeDecision(List<Stock> marketStocks) {
        Map<String, Double> decision = strategy.makeDecision(marketStocks, this);

        // Iterate over decision to buy or sell based on the amount to invest
        for (Stock stock : marketStocks) {
            String symbol = stock.getSymbol();
            double amountToInvest = decision.getOrDefault(symbol, 0.0);

            if (amountToInvest > 0) {
                buyStock(stock, amountToInvest);
            }
        }
    }

    // Buys stocks with the given amount of money
    private void buyStock(Stock stock, double amount) {
        int quantity = (int) (amount / stock.getCurrentPrice());  // Calculate how many shares can be bought

        if (quantity > 0 && cash >= amount) {
            cash -= quantity*stock.getCurrentPrice();  // Deduct the amount spent from cash
            portfolio.put(stock.getSymbol(), portfolio.getOrDefault(stock.getSymbol(), 0) + quantity);

        }
    }



    // Getters
    public String getName() { return name; }
    public double getCash() { return cash; }
    public Map<String, Integer> getPortfolio() { return portfolio; }

    public Double getStockTotalValue(Stock stock) {
        int quantity = portfolio.getOrDefault(stock.getSymbol(), 0);
        return stock.getCurrentPrice() * quantity;
    }


}
