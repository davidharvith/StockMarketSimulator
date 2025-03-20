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

    // Update the simulation date (could be daily, weekly, etc.)
    public void updateDate() {
        currentDate = currentDate.plusDays(1); // Increment by one day
    }

    // Update stock prices (this could be pulling data or using a model)
    public void updateStocks() {
        for (Stock stock : stocks) {
            stock.updatePrice(); // Stock class should have logic for price changes
        }
    }

    // Let all traders trade (buy/sell based on their strategy)
    public void runSimulation() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Map<String, Double>> portfolioValues = new TreeMap<>();

        // Ensure the simulation doesn't run beyond today's date
        while (!currentDate.isAfter(today)) {



            // Let each trader make their move
            for (Trader trader : traders) {

                trader.makeDecision(stocks); // Trader class should have logic for decision-making
            }

            // Track portfolio values on a monthly basis
            if (currentDate.getDayOfMonth() == 1) { // Only track on the first day of each month
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

        // Output portfolio values to CSV
        outputPortfolioValuesToCSV(portfolioValues);

        // Plot the portfolio values
        plotPortfolioValues(portfolioValues);

        // Once simulation is done, you can display or log the results
        System.out.println("Simulation complete up to " + today);
    }

    private double getPortfolioValue(Trader trader) {
        double result = trader.getCash();
        for(Stock stock: stocks){
            result+= trader.getStockTotalValue(stock);
        }
        return result;
    }

    private void outputPortfolioValuesToCSV(Map<LocalDate, Map<String, Double>> portfolioValues) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("portfolio_values.csv"))) {
            writer.write("Date,Trader,Portfolio Value\n");

            // Now entries are sorted by date since we're using TreeMap
            for (Map.Entry<LocalDate, Map<String, Double>> entry : portfolioValues.entrySet()) {
                LocalDate date = entry.getKey();
                for (Map.Entry<String, Double> traderEntry : entry.getValue().entrySet()) {
                    writer.write(date + "," + traderEntry.getKey() + "," + traderEntry.getValue() + "\n");
                }
            }
            System.out.println("Portfolio values saved to portfolio_values.csv");
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private void plotPortfolioValues(Map<LocalDate, Map<String, Double>> portfolioValues) {
        XYSeriesCollection dataset = getXySeriesCollection(portfolioValues);

        List<String> stockSymbols = getStockSymbols();
        String stocksUsed = String.join(", ", stockSymbols);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Trader Portfolio Values Over Time",
                "Date",
                "Portfolio Value",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        chart.addSubtitle(new org.jfree.chart.title.TextTitle("Stocks: " + stocksUsed));

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis dateAxis = new DateAxis("Date");

        // Find the first and last dates
        List<LocalDate> sortedDates = new ArrayList<>(portfolioValues.keySet());
        Collections.sort(sortedDates);
        if (!sortedDates.isEmpty()) {
            Date startDate = Date.from(sortedDates.get(0).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(sortedDates.get(sortedDates.size() - 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateAxis.setRange(startDate, endDate); // Set range to first and last date
        }

        plot.setDomainAxis(dateAxis);

        // Display chart in a JFrame
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Portfolio Value Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        // Save chart as an image
        try {
            File file = new File("portfolio_chart.png");
            ChartUtils.saveChartAsPNG(file, chart, 800, 600);
            System.out.println("Chart saved as portfolio_chart.png");
        } catch (IOException e) {
            System.out.println("Error saving chart: " + e.getMessage());
        }
    }

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

                series.add(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime(), portfolioValue);

            }
        }
        return dataset;
    }

    // Getters and setters
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
