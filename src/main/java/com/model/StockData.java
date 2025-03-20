package com.model;

import java.time.LocalDate;

public class StockData {
    private final LocalDate date;
    private final double price;

    // Constructor
    public StockData(LocalDate date, double price) {
        this.date = date;
        this.price = price;
    }

    // Getters
    public LocalDate getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }
}
