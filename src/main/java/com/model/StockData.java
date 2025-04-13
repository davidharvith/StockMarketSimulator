package com.model;

import java.time.LocalDate;

/**
 * Represents a single entry of historical stock data.
 * Contains the stock price and the corresponding date.
 */
public class StockData {
    /** The date of the stock data point. */
    private final LocalDate date;

    /** The closing price of the stock on the given date. */
    private final double price;

    /**
     * Constructs a StockData object with a date and price.
     *
     * @param date  The date associated with the stock price.
     * @param price The closing price of the stock on that date.
     */
    public StockData(LocalDate date, double price) {
        this.date = date;
        this.price = price;
    }

    /**
     * Gets the date of this stock data.
     *
     * @return The date.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the stock price on the corresponding date.
     *
     * @return The closing price.
     */
    public double getPrice() {
        return price;
    }
}
