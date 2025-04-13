package com.simulation;

import com.model.Stock;
import com.model.Trader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class StockExchange {
    private static final int SIMULATION_STEP_DAYS = 1;
    private static final int MONTH_START_DAY = 1;
    private static final String CSV_OUTPUT_PATH = "portfolio_values.csv";
    private static final String CSV_HEADER = "Date,Trader,Portfolio Value\n";
    private static final String PNG_OUTPUT_PATH = "portfolio_chart.png";
    private static final String CHART_TITLE = "Trader Portfolio Values Over Time";
    private static final String CHART_X_LABEL = "Date";
    private static final String CHART_Y_LABEL = "Portfolio Value";
    private static final String FRAME_TITLE = "Portfolio Value Plot";

    private final List<Stock> stocks;
    private final List<Trader> traders;
    private LocalDate currentDate;
    private final LocalDate startDate;

    public StockExchange(List<Stock> stocks, List<Trader> traders, LocalDate startDate) {
        this.stocks = stocks;
        this.traders = traders;
        this.startDate = startDate;
        this.currentDate = startDate;
    }

    /**
     * Advances the simulation date by one day.
     */
    public void updateDate() {
        currentDate = currentDate.plusDays(SIMULATION_STEP_DAYS);
    }

    /**
     * Updates all stock prices.
     */
    public void updateStocks() {
        for (Stock stock : stocks) {
            stock.updatePrice();
        }
    }

    /**
     * Runs the simulation day-by-day until the present date.
     * Traders make decisions and portfolio values are tracked monthly.
     * At the end, results are output to CSV and plotted.
     */
    public void runSimulation() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Map<String, Double>> portfolioValues = new TreeMap<>();

        while (!currentDate.isAfter(today)) {
            for (Trader trader : traders) {
                trader.makeDecision(stocks);
            }

            if (currentDate.getDayOfMonth() == MONTH_START_DAY) {
                Map<String, Double> monthlyValues = new HashMap<>();
                for (Trader trader : traders) {
                    double portfolioValue = getPortfolioValue(trader);
                    monthlyValues.put(trader.getName(), portfolioValue);
                }
                portfolioValues.put(currentDate, monthlyValues);
            }

            updateDate();
            updateStocks();
        }

        outputPortfolioValuesToCSV(portfolioValues);
        plotPortfolioValues(portfolioValues);

        System.out.println("Simulation complete up to " + today);
    }

    /**
     * Calculates the total value of a trader's portfolio.
     */
    private double getPortfolioValue(Trader trader) {
        double result = trader.getCash();
        for (Stock stock : stocks) {
            result += trader.getStockTotalValue(stock);
        }
        return result;
    }

    /**
     * Outputs portfolio values to a CSV file.
     */
    private void outputPortfolioValuesToCSV(Map<LocalDate, Map<String, Double>> portfolioValues) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_OUTPUT_PATH))) {
            writer.write(CSV_HEADER);
            for (Map.Entry<LocalDate, Map<String, Double>> entry : portfolioValues.entrySet()) {
                LocalDate date = entry.getKey();
                for (Map.Entry<String, Double> traderEntry : entry.getValue().entrySet()) {
                    writer.write(date + "," + traderEntry.getKey() + "," + traderEntry.getValue() + "\n");
                }
            }
            System.out.println("Portfolio values saved to " + CSV_OUTPUT_PATH);
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    /**
     * Plots the portfolio values using JFreeChart.
     */
    private void plotPortfolioValues(Map<LocalDate, Map<String, Double>> portfolioValues) {
        XYSeriesCollection dataset = getXySeriesCollection(portfolioValues);
        List<String> stockSymbols = getStockSymbols();
        String stocksUsed = String.join(", ", stockSymbols);

        JFreeChart chart = ChartFactory.createXYLineChart(
                CHART_TITLE,
                CHART_X_LABEL,
                CHART_Y_LABEL,
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.addSubtitle(new org.jfree.chart.title.TextTitle("Stocks: " + stocksUsed));

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis dateAxis = new DateAxis(CHART_X_LABEL);

        List<LocalDate> sortedDates = new ArrayList<>(portfolioValues.keySet());
        Collections.sort(sortedDates);
        if (!sortedDates.isEmpty()) {
            Date startDate = Date.from(sortedDates.get(0).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(sortedDates.get(sortedDates.size() - 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateAxis.setRange(startDate, endDate);
        }

        plot.setDomainAxis(dateAxis);

        JFrame frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);

        try {
            ChartUtils.saveChartAsPNG(new File(PNG_OUTPUT_PATH), chart, 800, 600);
            System.out.println("Chart saved as " + PNG_OUTPUT_PATH);
        } catch (IOException e) {
            System.out.println("Error saving chart: " + e.getMessage());
        }
    }

    /**
     * Converts portfolio values into a dataset for plotting.
     */
    private static XYSeriesCollection getXySeriesCollection(Map<LocalDate, Map<String, Double>> portfolioValues) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Map<String, XYSeries> seriesMap = new HashMap<>();

        List<LocalDate> sortedDates = new ArrayList<>(portfolioValues.keySet());
        Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            for (Map.Entry<String, Double> traderEntry : portfolioValues.get(date).entrySet()) {
                String traderName = traderEntry.getKey();
                Double portfolioValue = traderEntry.getValue();

                XYSeries series = seriesMap.computeIfAbsent(traderName, k -> {
                    XYSeries newSeries = new XYSeries(traderName);
                    dataset.addSeries(newSeries);
                    return newSeries;
                });

                long timestamp = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
                series.add(timestamp, portfolioValue);
            }
        }

        return dataset;
    }

    // Getters
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<String> getStockSymbols() {
        return stocks.stream().map(Stock::getSymbol).collect(Collectors.toList());
    }
}
