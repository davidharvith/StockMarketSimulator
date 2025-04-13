package com.model;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents a stock with historical price data and technical analysis capabilities.
 */
public class Stock {

    private static final int DEFAULT_RSI_PERIOD = 14;

    private final String symbol;
    private final List<StockData> historicalData;  // List of historical stock data (price, date)
    private double currentPrice;
    private int index = 0;
    private BarSeries series; // ta4j series for technical indicators
    private RSIIndicator rsiIndicator; // ta4j RSI indicator

    /**
     * Constructs a Stock object with a symbol and historical price data.
     *
     * @param symbol         Stock symbol (e.g., "AAPL", "GOOG")
     * @param historicalData List of historical stock data
     */
    public Stock(String symbol, List<StockData> historicalData) {
        this.symbol = symbol;
        this.historicalData = historicalData;
        if (!historicalData.isEmpty()) {
            this.currentPrice = historicalData.get(index).getPrice();
            this.series = new BaseBarSeries();

            for (StockData data : historicalData) {
                ZonedDateTime dateTime = data.getDate().atStartOfDay(ZoneId.systemDefault());
                this.series.addBar(dateTime, 0, 0, 0, data.getPrice(), 0);
            }

            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            this.rsiIndicator = new RSIIndicator(closePrice, DEFAULT_RSI_PERIOD);
        }
    }

    /**
     * Returns the current price of the stock.
     *
     * @return Current stock price
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the current price of the stock.
     *
     * @param currentPrice New stock price
     */
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    /**
     * Updates the stock to the next day's price and advances the index.
     */
    public void updatePrice() {
        if (index < historicalData.size() - 1) {
            index += 1;
            StockData nextData = historicalData.get(index);
            currentPrice = nextData.getPrice();
            rsiIndicator.getValue(index); // Access to update internal state
        }
    }

    /**
     * Calculates the moving average over the given number of days.
     *
     * @param days Number of days for the moving average
     * @return Moving average value
     */
    public double calculateMovingAverage(int days) {
        int startIndex = Math.max(0, index - days);
        double sum = 0;
        for (int i = startIndex; i <= index; i++) {
            sum += historicalData.get(i).getPrice();
        }
        return sum / (index - startIndex + 1);
    }

    /**
     * Calculates the volatility (standard deviation) over a given number of days.
     *
     * @param days Number of days for volatility calculation
     * @return Volatility value
     */
    public double calculateVolatility(int days) {
        return calculateStandardDeviation(days);
    }


    /**
     * Returns the stock's symbol.
     *
     * @return Stock symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the historical data list.
     *
     * @return List of historical stock data
     */
    public List<StockData> getHistoricalData() {
        return historicalData;
    }

    /**
     * Calculates the Relative Strength Index (RSI) for the current index.
     *
     * @param period RSI period to calculate
     * @return RSI value
     */
    public double calculateRSI(int period) {
        if (rsiIndicator == null || period != DEFAULT_RSI_PERIOD) {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            rsiIndicator = new RSIIndicator(closePrice, period);
        }

        if (index < period) {
            return 50.0; // Neutral RSI if not enough data
        }

        return rsiIndicator.getValue(index).doubleValue();
    }

    /**
     * Calculates the standard deviation of prices over a given number of days.
     *
     * @param days Number of days to calculate standard deviation
     * @return Standard deviation value
     */
    public double calculateStandardDeviation(int days) {
        int startIndex = Math.max(0, index - days);
        double mean = calculateMovingAverage(days);
        double sumSquaredDifferences = 0;

        for (int i = startIndex; i <= index; i++) {
            double diff = historicalData.get(i).getPrice() - mean;
            sumSquaredDifferences += diff * diff;
        }

        return Math.sqrt(sumSquaredDifferences / (index - startIndex));
    }
}
