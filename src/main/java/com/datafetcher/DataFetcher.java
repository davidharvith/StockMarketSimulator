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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Fetches stock data from the AlphaVantage API and S&P 500 symbols from Wikipedia.
 */
public class DataFetcher {

    private static final String API_KEY = "WK3R79SXV9RXQO23";
    private static final int API_TIMEOUT_SECONDS = 10;
    private static final String WIKI_URL = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies";
    private static final String TABLE_SELECTOR = "table.wikitable";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ERROR_MSG_API_NULL = "Unknown error occurred";
    private static final String ERROR_MSG_RATE_LIMIT = "rate limit";
    private static final String LOG_FETCHED_SYMBOLS = "✔ Successfully fetched %d S&P 500 symbols from Wikipedia.";
    private static final String LOG_FETCHED_STOCK = "✔ Fetched data for %s";
    private static final String LOG_NO_DATA = "⚠ No data found for %s";
    private static final String LOG_ERROR_FETCHING = "⚠ Error fetching S&P 500 symbols from Wikipedia: ";

    private final Set<String> validStockSymbols;

    /**
     * Initializes the AlphaVantage API and fetches valid S&P 500 stock symbols.
     */
    public DataFetcher() {
        Config cfg = Config.builder()
                .key(API_KEY)
                .timeOut(API_TIMEOUT_SECONDS)
                .build();

        AlphaVantage.api().init(cfg);
        this.validStockSymbols = fetchSPSymbolsFromWiki();
    }

    /**
     * Scrapes Wikipedia to retrieve a list of S&P 500 stock symbols.
     *
     * @return Set of valid stock symbols
     */
    private Set<String> fetchSPSymbolsFromWiki() {
        Set<String> symbols = new HashSet<>();
        try {
            Document doc = Jsoup.connect(WIKI_URL).get();
            Element table = doc.select(TABLE_SELECTOR).first();
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
            System.out.printf((LOG_FETCHED_SYMBOLS) + "%n", symbols.size());
        } catch (Exception e) {
            System.err.println(LOG_ERROR_FETCHING + e.getMessage());
        }
        return symbols;
    }

    /**
     * Checks if a given symbol is a valid S&P 500 symbol.
     *
     * @param symbol The stock symbol to validate
     * @return true if the symbol is valid, false otherwise
     */
    public boolean isValidStockSymbol(String symbol) {
        return validStockSymbols.contains(symbol);
    }

    /**
     * Fetches historical stock data for the given list of symbols over a period of years.
     *
     * @param stockSymbols List of stock symbols to fetch
     * @param yearsBack    Number of years of historical data to retrieve
     * @return List of Stock objects containing historical data
     * @throws APILimitExceededException if the API rate limit is exceeded
     */
    public List<Stock> fetchHistoricalData(List<String> stockSymbols, int yearsBack) throws APILimitExceededException {
        List<Stock> stocks = new ArrayList<>();
        LocalDate cutoffDate = LocalDate.now().minusYears(yearsBack);

        for (String symbol : stockSymbols) {
            TimeSeriesResponse response = AlphaVantage.api()
                    .timeSeries()
                    .daily()
                    .forSymbol(symbol)
                    .outputSize(OutputSize.FULL)
                    .fetchSync();

            checkAPIValidity(symbol, response);

            List<StockUnit> timeSeries = response.getStockUnits();
            if (timeSeries != null) {
                List<StockData> stockDataList = new ArrayList<>();
                fillStockDataList(timeSeries, cutoffDate, stockDataList);

                List<StockData> reversedData = new ArrayList<>(stockDataList);
                Collections.reverse(reversedData);

                System.out.printf((LOG_FETCHED_STOCK) + "%n", symbol);
                stocks.add(new Stock(symbol, reversedData));
            } else {
                System.out.printf((LOG_NO_DATA) + "%n", symbol);
            }
        }

        return stocks;
    }

    /**
     * Populates a list with StockData entries from the API response.
     *
     * @param timeSeries     List of StockUnit entries
     * @param cutoffDate     Date to filter old entries
     * @param stockDataList  Output list to populate
     */
    private static void fillStockDataList(List<StockUnit> timeSeries, LocalDate cutoffDate, List<StockData> stockDataList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        for (StockUnit entry : timeSeries) {
            LocalDate date = LocalDate.parse(entry.getDate(), formatter);
            if (!date.isBefore(cutoffDate)) {
                stockDataList.add(new StockData(date, entry.getClose()));
            }
        }
    }

    /**
     * Validates the API response and handles errors.
     *
     * @param symbol   The stock symbol being queried
     * @param response API response object
     * @throws APILimitExceededException if the API rate limit is exceeded
     */
    private static void checkAPIValidity(String symbol, TimeSeriesResponse response) throws APILimitExceededException {
        if (response == null || response.getErrorMessage() != null) {
            String errorMessage = response != null ? response.getErrorMessage() : ERROR_MSG_API_NULL;

            if (errorMessage.toLowerCase().contains(ERROR_MSG_RATE_LIMIT)) {
                throw new APILimitExceededException("API rate limit exceeded: " + errorMessage);
            } else {
                throw new RuntimeException("Failed to fetch data for " + symbol + ": " + errorMessage);
            }
        }
    }

    /**
     * Custom exception to indicate that the API rate limit was exceeded.
     */
    public static class APILimitExceededException extends Exception {
        public APILimitExceededException(String message) {
            super(message);
        }
    }
}
