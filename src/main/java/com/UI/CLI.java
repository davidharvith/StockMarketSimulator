package com.UI;

import com.datafetcher.DataFetcher;
import com.model.Stock;
import com.model.Trader;
import com.simulation.StockExchange;
import com.strategy.Strategy;
import com.strategy.StrategyFactory;

import java.time.LocalDate;
import java.util.*;

/**
 * Command Line Interface for running the stock trading simulation.
 */
public class CLI {

    private static final int MAX_STOCKS = 25;
    private static final String SYMBOL_PROMPT = "Enter stock symbols separated by spaces (up to %d stocks), press enter when done:";
    private static final String INVALID_SYMBOL_MSG = "❌ Invalid stock symbol: ";
    private static final String SYMBOL_LIMIT_MSG = "⚠️ Please enter up to %d valid stock symbols.";
    private static final String STRATEGY_COUNT_PROMPT = "How many strategies do you want to use?";
    private static final String STRATEGY_NAME_PROMPT = "Enter strategy %d name. Choose from the following: ";
    private static final String INVALID_STRATEGY_MSG = "Invalid strategy name. Please choose a valid strategy.";
    private static final String SIM_DURATION_PROMPT = "Enter the number of years for the simulation:";
    private static final String STRATEGY_STARTING_MONEY_PROMPT = "Enter starting money for trader using strategy %s:";
    private static final String POSITIVE_NUMBER_MSG = "Please enter a positive number";
    private static final String INVALID_NUMBER_MSG = "Invalid input. Please enter a valid number.";

    public static void main(String[] args) throws DataFetcher.APILimitExceededException {
        Scanner scanner = new Scanner(System.in);
        DataFetcher dataFetcher = new DataFetcher();

        List<String> stockSymbols = getStockSymbols(scanner, dataFetcher);
        int strategyCount = getStrategyCount(scanner);
        List<Strategy> strategies = getStrategies(scanner, strategyCount);
        int yearsBack = getSimulationDuration(scanner);
        List<Stock> stocks = dataFetcher.fetchHistoricalData(stockSymbols, yearsBack);
        List<Trader> traders = getTraders(scanner, strategies);

        StockExchange exchange = new StockExchange(stocks, traders, LocalDate.now().minusYears(yearsBack));
        exchange.runSimulation();

        scanner.close();
    }

    /**
     * Prompts user to input valid stock symbols.
     */
    private static List<String> getStockSymbols(Scanner scanner, DataFetcher dataFetcher) {
        List<String> stockSymbols;

        while (true) {
            stockSymbols = new ArrayList<>();
            System.out.printf((SYMBOL_PROMPT) + "%n", MAX_STOCKS);

            String[] inputs = scanner.nextLine().split(" ");
            boolean allValid = true;

            for (String symbol : inputs) {
                if (dataFetcher.isValidStockSymbol(symbol)) {
                    stockSymbols.add(symbol);
                } else {
                    System.out.println(INVALID_SYMBOL_MSG + symbol);
                    allValid = false;
                }
            }

            if (allValid && !stockSymbols.isEmpty() && stockSymbols.size() <= MAX_STOCKS) {
                break;
            }

            System.out.printf((SYMBOL_LIMIT_MSG) + "%n", MAX_STOCKS);
        }

        return stockSymbols;
    }

    /**
     * Prompts user to input number of strategies to use.
     */
    private static int getStrategyCount(Scanner scanner) {
        int strategyCount;
        while (true) {
            System.out.println(STRATEGY_COUNT_PROMPT);
            try {
                strategyCount = Integer.parseInt(scanner.nextLine());
                if (strategyCount > 0) {
                    break;
                } else {
                    System.out.println(POSITIVE_NUMBER_MSG + " for the strategy count.");
                }
            } catch (NumberFormatException e) {
                System.out.println(INVALID_NUMBER_MSG);
            }
        }
        return strategyCount;
    }

    /**
     * Prompts user to select strategy names.
     */
    private static List<Strategy> getStrategies(Scanner scanner, int strategyCount) {
        List<Strategy> strategies = new ArrayList<>();
        for (int i = 0; i < strategyCount; i++) {
            while (true) {
                System.out.println(String.format(STRATEGY_NAME_PROMPT, i + 1) + StrategyFactory.getAvailableStrategiesNames());
                String strategyName = scanner.nextLine();
                Strategy strategy = StrategyFactory.create(strategyName);
                if (strategy != null) {
                    strategies.add(strategy);
                    break;
                } else {
                    System.out.println(INVALID_STRATEGY_MSG);
                }
            }
        }
        return strategies;
    }

    /**
     * Prompts user for simulation duration in years.
     */
    private static int getSimulationDuration(Scanner scanner) {
        int yearsBack;
        while (true) {
            System.out.println(SIM_DURATION_PROMPT);
            try {
                yearsBack = Integer.parseInt(scanner.nextLine());
                if (yearsBack > 0) {
                    break;
                } else {
                    System.out.println(POSITIVE_NUMBER_MSG + " for the number of years.");
                }
            } catch (NumberFormatException e) {
                System.out.println(INVALID_NUMBER_MSG);
            }
        }
        return yearsBack;
    }

    /**
     * Prompts user to enter trader starting money for each strategy.
     */
    private static List<Trader> getTraders(Scanner scanner, List<Strategy> strategies) {
        List<Trader> traders = new ArrayList<>();
        for (Strategy strategy : strategies) {
            while (true) {
                System.out.printf((STRATEGY_STARTING_MONEY_PROMPT) + "%n", strategy.getName());
                try {
                    double startingMoney = Double.parseDouble(scanner.nextLine());
                    if (startingMoney > 0) {
                        traders.add(new Trader(strategy, startingMoney));
                        break;
                    } else {
                        System.out.println("Starting money must be positive. " + POSITIVE_NUMBER_MSG + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(INVALID_NUMBER_MSG);
                }
            }
        }
        return traders;
    }
}
