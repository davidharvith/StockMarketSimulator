package com.model;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class Stock {

    private final String symbol;
    private final List<StockData> historicalData;  // List of historical stock data (price, date)
    private double currentPrice;
    private int indx = 0;
    private BarSeries series; // ta4j series
    private RSIIndicator rsiIndicator; // ta4j RSI indicator

    // Constructor to initialize with historical data
    public Stock(String symbol, List<StockData> historicalData) {
        this.symbol = symbol;
        this.historicalData = historicalData;
        if (!historicalData.isEmpty()) {
            this.currentPrice = historicalData.get(indx).getPrice();
            this.series = new BaseBarSeries();

            // Convert your data to ta4j bars and add them to the series
            for (StockData data : historicalData) {
                ZonedDateTime dateTime = data.getDate().atStartOfDay(ZoneId.systemDefault());
                this.series.addBar(dateTime, 0, 0, 0, data.getPrice(), 0);
            }

            // Create RSI indicator based on the series
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            this.rsiIndicator = new RSIIndicator(closePrice, 14); // Default period of 14
        }
    }

    // Get the current stock price
    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    // Update the stock price to the next day's price
    public void updatePrice() {
        // Check if there's still data left to update
        if (indx < historicalData.size() - 1) {
            // Increment index and get the next day's stock data
            indx += 1;
            StockData nextData = historicalData.get(indx);
            currentPrice = nextData.getPrice();

            // Recalculate RSI for the current index
            rsiIndicator.getValue(indx);
        }
    }

    // Calculate the moving average for a given number of days
    public double calculateMovingAverage(int days) {
        int startIndex = Math.max(0, indx - days);
        double sum = 0;
        for (int i = startIndex; i < indx + 1; i++) {
            sum += historicalData.get(i).getPrice();
        }
        return sum / (indx - startIndex + 1);
    }

    // Calculate the volatility of the stock price over a given period
    public double calculateVolatility(int days) {
        int startIndex = Math.max(0, indx - days);
        double average = calculateMovingAverage(days);
        double sumSquaredDifferences = 0;
        for (int i = startIndex; i < indx + 1; i++) {
            double diff = historicalData.get(i).getPrice() - average;
            sumSquaredDifferences += diff * diff;
        }
        return Math.sqrt(sumSquaredDifferences / days);
    }

    // Get the symbol of the stock (e.g., "AAPL", "GOOG")
    public String getSymbol() {
        return symbol;
    }

    // Get historical data for analysis
    public List<StockData> getHistoricalData() {
        return historicalData;
    }


    // Calculate the RSI for the current period (given an RSI period)
    public double calculateRSI(int period) {
        // If period is different from current indicator, create a new one
        if (rsiIndicator == null || period != 14) { // Assuming 14 was the default
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            rsiIndicator = new RSIIndicator(closePrice, period);
        }

        // Use ta4j to calculate RSI at current index
        if (indx < period) {
            return 50.0; // Return neutral RSI if not enough data
        }

        return rsiIndicator.getValue(indx).doubleValue();
    }

    // Calculate the standard deviation for the stock price over a given period
    public double calculateStandardDeviation(int days) {
        int startIndex = Math.max(0, historicalData.size() - days);
        double mean = calculateMovingAverage(days);
        double sumSquaredDifferences = 0;

        for (int i = startIndex; i < historicalData.size(); i++) {
            double diff = historicalData.get(i).getPrice() - mean;
            sumSquaredDifferences += diff * diff;
        }

        return Math.sqrt(sumSquaredDifferences / (historicalData.size() - startIndex - 1));
    }
}
