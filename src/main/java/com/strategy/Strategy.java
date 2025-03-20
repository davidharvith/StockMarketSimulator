package com.strategy;

import com.model.Stock;
import com.model.Trader;

import java.util.Map;
import java.util.List;

public interface Strategy {
    /**
     * Makes a trading decision (how much money to invest in each stock) based on the current market and the trader's portfolio.
     * @param marketStocks A list of all available stocks in the market.
     * @param trader The trader who is making the decision.
     * @return A Map where the key is the stock symbol and the value is the amount of money to invest in that stock.
     */
    Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader);

    String getName();
}
