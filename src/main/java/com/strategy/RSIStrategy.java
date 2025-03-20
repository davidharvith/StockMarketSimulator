package com.strategy;

import com.model.Stock;
import com.model.Trader;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class RSIStrategy implements Strategy {

    private static final double RSIMAX = 70;
    private static final int PERIOD = 14;
    private static final double RSIMIN = 30;
    private final String NAME = "RSI Strategy";

    @Override
    public Map<String, Double> makeDecision(List<Stock> marketStocks, Trader trader) {
        Map<String, Double> decision = new HashMap<>();

        // Sell if RSI is greater than 70 (overbought)
        for (Stock stock : marketStocks) {
            assert stock != null;
            double rsi = stock.calculateRSI(PERIOD);  // Assuming 14-day RSI period

            // Sell condition: RSI > 70
            if (rsi > RSIMAX) {
                decision.put(stock.getSymbol(), -trader.getStockTotalValue(stock));  // Sell all of this stock
            }
        }

        // Buy if RSI is less than 30 (oversold)
        double availableCash = trader.getCash();
        int numStocksToBuy = 0;

        // Count how many stocks to buy based on the RSI condition
        for (Stock stock : marketStocks) {
            double rsi = stock.calculateRSI(PERIOD);

            if (rsi < RSIMIN) {
                numStocksToBuy++;
            }
        }

        // Split the available cash equally for buying the stocks we want to buy
        if (numStocksToBuy > 0) {
            double cashPerStock = availableCash / numStocksToBuy;

            // Allocate the available cash to the stocks we want to buy
            for (Stock stock : marketStocks) {
                double rsi = stock.calculateRSI(PERIOD);

                if (rsi < RSIMIN) {
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
