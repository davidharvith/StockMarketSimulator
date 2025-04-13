package com.model;

import com.strategy.Strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Represents a trader that follows a specific investment strategy.
 * Each trader has a portfolio, available cash, and can make buy decisions based on their strategy.
 */
public class Trader {

    /** The investment strategy followed by the trader. */
    private final Strategy strategy;

    /** The name of the trader, derived from the strategy and initial cash. */
    private final String name;

    /** Available cash for trading. */
    private double cash;

    /** Portfolio mapping stock symbols to the number of shares owned. */
    private final Map<String, Integer> portfolio;

    /**
     * Constructs a Trader with a given strategy and initial amount of cash.
     *
     * @param strategy    The trading strategy used to make decisions.
     * @param initialCash The initial amount of cash available to the trader.
     */
    public Trader(Strategy strategy, double initialCash) {
        this.strategy = strategy;
        this.name = strategy.getClass().getSimpleName() + " with " + initialCash;
        this.cash = initialCash;
        this.portfolio = new HashMap<>();
    }

    /**
     * Executes the trader's decision to buy stocks based on their strategy.
     * Only handles buying logic; selling can be added in future.
     *
     * @param marketStocks List of stocks in the market to consider.
     */
    public void makeDecision(List<Stock> marketStocks) {
        Map<String, Double> decision = strategy.makeDecision(marketStocks, this);

        for (Stock stock : marketStocks) {
            String symbol = stock.getSymbol();
            double amountToInvest = decision.getOrDefault(symbol, 0.0);

            if (amountToInvest > 0) {
                buyStock(stock, amountToInvest);
            }
        }
    }

    /**
     * Executes a buy order for a given stock and amount.
     * Calculates how many whole shares can be bought and updates the portfolio and cash balance.
     *
     * @param stock  The stock to buy.
     * @param amount The amount of money to invest in the stock.
     */
    private void buyStock(Stock stock, double amount) {
        double price = stock.getCurrentPrice();
        int quantity = (int) (amount / price);

        if (quantity > 0 && cash >= quantity * price) {
            cash -= quantity * price;
            portfolio.put(stock.getSymbol(),
                    portfolio.getOrDefault(stock.getSymbol(), 0) + quantity);
        }
    }

    /**
     * Returns the name of the trader.
     *
     * @return Trader name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the trader's available cash.
     *
     * @return Available cash.
     */
    public double getCash() {
        return cash;
    }

    /**
     * Gets the trader's current portfolio.
     *
     * @return Map of stock symbols to number of shares owned.
     */
    public Map<String, Integer> getPortfolio() {
        return portfolio;
    }

    /**
     * Calculates the total value of a given stock in the trader's portfolio.
     *
     * @param stock The stock to evaluate.
     * @return Total value of owned shares of the stock.
     */
    public double getStockTotalValue(Stock stock) {
        int quantity = portfolio.getOrDefault(stock.getSymbol(), 0);
        return stock.getCurrentPrice() * quantity;
    }
}
