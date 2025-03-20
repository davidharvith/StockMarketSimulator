package com.datafetcher;

import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.model.Stock;
import com.model.StockData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DataFetcher {
    private static final String API_KEY = "WK3R79SXV9RXQO23";

    public DataFetcher() {
        Config cfg = Config.builder()
                .key(API_KEY)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
    }

    public List<Stock> fetchHistoricalData(List<String> stockSymbols, int yearsBack) throws APILimitExceededException {
        List<Stock> stocks = new ArrayList<>();

        // Calculate the cut-off date based on the yearsBack parameter
        LocalDate cutoffDate = LocalDate.now().minusYears(yearsBack);

        for (String symbol : stockSymbols) {
            // Fetch the historical data from AlphaVantage API
            TimeSeriesResponse response = AlphaVantage.api()
                    .timeSeries()
                    .daily()
                    .forSymbol(symbol)
                    .outputSize(OutputSize.FULL)
                    .fetchSync();

            // Check if the response contains an error message
            if (response == null || response.getErrorMessage() != null) {
                String errorMessage = response != null ? response.getErrorMessage() : "Unknown error occurred";

                // Check if the error message is related to rate limits
                if (errorMessage.toLowerCase().contains("rate limit")) {
                    throw new APILimitExceededException("API rate limit exceeded: " + errorMessage);
                } else {
                    throw new RuntimeException("Failed to fetch data for " + symbol + ": " + errorMessage);
                }
            }

            // Extract the time series data
            List<StockUnit> timeSeries = response.getStockUnits();

            if (timeSeries != null) {
                // Create a list to hold the stock data (StockData objects)
                List<StockData> stockDataList = new ArrayList<>();

                // Loop through the time series and create StockData objects
                for (StockUnit entry : timeSeries) {
                    String dateString = entry.getDate();  // The date is the key (e.g., "2024-10-23")

                    double closePrice = entry.getClose();  // Get the close price
                    // Convert the string date to LocalDate
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    // Only add data that's within the last 'yearsBack' years
                    if (!date.isBefore(cutoffDate)) {
                        // Create StockData object and add it to the list
                        StockData stockData = new StockData(date, closePrice);
                        stockDataList.add(stockData);
                    }
                }

                List<StockData> reversedData = new ArrayList<>(stockDataList); // Create a mutable copy
                Collections.reverse(reversedData); // Reverse the list


                // Create the Stock object and add it to the stocks list
                System.out.println("Fetched data for " + symbol);

                Stock stock = new Stock(symbol, reversedData);
                stocks.add(stock);
            } else {
                System.out.println("No data found for " + symbol);
            }
        }

        return stocks;
    }

    // Custom Exception for API Rate Limit Exceeded
    public static class APILimitExceededException extends Exception {
        public APILimitExceededException(String message) {
            super(message);
        }
    }


}
