package com.datafetcher;

import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.model.Stock;
import com.model.StockData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataFetcher {
    private static final String API_KEY = "WK3R79SXV9RXQO23";
    private static final String WIKI_URL = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies";
    private Set<String> validStockSymbols;

    public DataFetcher() {
        Config cfg = Config.builder()
                .key(API_KEY)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
        this.validStockSymbols = fetchSPSymbolsFromWiki();
    }

    // 🛠️ Scrape S&P 500 symbols from Wikipedia
    private Set<String> fetchSPSymbolsFromWiki() {
        Set<String> symbols = new HashSet<>();
        try {
            // Fetch and parse the Wikipedia page
            Document doc = Jsoup.connect(WIKI_URL).get();

            // Locate the table containing stock symbols
            Element table = doc.select("table.wikitable").first();
            if (table != null) {
                Elements rows = table.select("tbody tr");

                for (Element row : rows) {
                    Elements columns = row.select("td");
                    if (!columns.isEmpty()) {
                        String symbol = columns.get(0).text().trim();
                        symbols.add(symbol);
                    }
                }
            }
            System.out.println("✔ Successfully fetched " + symbols.size() + " S&P 500 symbols from Wikipedia.");
        } catch (Exception e) {
            System.err.println("⚠ Error fetching S&P 500 symbols from Wikipedia: " + e.getMessage());
        }
        return symbols;
    }

    public boolean isValidStockSymbol(String symbol) {
        return validStockSymbols.contains(symbol);
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
                System.out.println("✔ Fetched data for " + symbol);

                Stock stock = new Stock(symbol, reversedData);
                stocks.add(stock);
            } else {
                System.out.println("⚠ No data found for " + symbol);
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
