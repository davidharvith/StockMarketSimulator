import com.model.Stock;
import com.model.StockData;
import org.junit.jupiter.api.Test;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static org.junit.jupiter.api.Assertions.*;

public class StockTest {
    private static final int LENHISTORY = 1000;
    private static final double MAXPRICE = 1000;
    private static final String STARTDATE = "2000-01-01";
    private Random rand = new Random();


    @Test
    public void testMovingAverageCalculation() {
        List<StockData> history = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(STARTDATE);
        double sum = 0;
        for(int i = 0; i<LENHISTORY; i++){
            double toAdd = rand.nextDouble()*MAXPRICE;
            sum += toAdd;
            history.add(new StockData(startDate.plusDays(i), toAdd));
        }

        Stock stock = new Stock("AAPL", history);
        for(int i = 0; i<LENHISTORY-1; i++){
            stock.updatePrice();
        }
        assertEquals(sum/LENHISTORY, stock.calculateMovingAverage(LENHISTORY), 0.001);
    }

    @Test
    public void testStockPriceUpdate() {
        List<StockData> history = new ArrayList<>(List.of(
                new StockData(LocalDate.parse("2024-03-01"), 100),
                new StockData(LocalDate.parse("2024-03-02"), 110)
        ));

        Stock stock = new Stock("AAPL", history);
        assertEquals(100, stock.getCurrentPrice());

        stock.updatePrice();
        assertEquals(110, stock.getCurrentPrice());
    }

    @Test
    public void testVolatilityCalculation() {
        List<StockData> history = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(STARTDATE);

        for(int i = 0; i<LENHISTORY; i++){
            double toAdd = rand.nextDouble()*MAXPRICE;
            history.add(new StockData(startDate.plusDays(i), toAdd));
        }

        Stock stock = new Stock("AAPL", history);
        for(int i = 0; i<LENHISTORY-1; i++){
            stock.updatePrice();
        }
        double volatility = stock.calculateVolatility(LENHISTORY);
        assertTrue(volatility > 0);
    }

    @Test
    public void testRSICalculation() {
        List<StockData> history = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(STARTDATE);
        BarSeries series = new BaseBarSeries();

        // Use the same seed for random to get consistent results


        // Generate consistent data for both implementations
        for (int i = 0; i < LENHISTORY; i++) {
            double toAdd = rand.nextDouble() * MAXPRICE;
            series.addBar(ZonedDateTime.now().plusDays(i), 0, 0, 0, toAdd, 0);
            history.add(new StockData(startDate.plusDays(i), toAdd));
        }

        Stock stock = new Stock("AAPL", history);

        // Update stock to process all data
        for(int i = 0; i < LENHISTORY-1; i++) {
            stock.updatePrice();
        }

        // Use the same period for both calculations
        int period = 14; // Standard RSI period

        // Compute RSI using TA4J
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator actualRsi = new RSIIndicator(closePrice, period);

        // Convert TA4J Num to double
        double actualRsiValue = actualRsi.getValue(series.getEndIndex()).doubleValue();

        // Compute RSI using your Stock class with the same period
        double rsi = stock.calculateRSI(period);

        assertEquals(actualRsiValue, rsi, 0.01);
    }

    @Test
    public void testRSICalculationWithNotEnoughData() {
        List<StockData> history = new ArrayList<>(List.of(
                new StockData(LocalDate.parse("2024-03-01"), 100),
                new StockData(LocalDate.parse("2024-03-02"), 105)
        ));

        Stock stock = new Stock("AAPL", history);
        assertEquals(50, stock.calculateRSI(14)); // Should return the default neutral RSI
    }

    @Test
    public void testStandardDeviationCalculation() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        List<StockData> history = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(STARTDATE);
        for(int i = 0; i<LENHISTORY; i++){
            double toAdd = rand.nextDouble()*MAXPRICE;
            stats.addValue(toAdd);
            history.add(new StockData(startDate.plusDays(i), toAdd));
        }

        Stock stock = new Stock("AAPL", history);
        for(int i = 0; i<LENHISTORY-1; i++){
            stock.updatePrice();

        }
        double stdDev = stock.calculateStandardDeviation(LENHISTORY);

        assertEquals(stdDev ,stats.getStandardDeviation(),0.01);
    }

    @Test
    public void testGetSymbol() {
        Stock stock = new Stock("GOOG", new ArrayList<>());
        assertEquals("GOOG", stock.getSymbol());
    }

    @Test
    public void testGetHistoricalData() {
        List<StockData> history = new ArrayList<>(List.of(
                new StockData(LocalDate.parse("2024-03-01"), 100),
                new StockData(LocalDate.parse("2024-03-02"), 110)
        ));

        Stock stock = new Stock("AAPL", history);
        assertEquals(history, stock.getHistoricalData());
    }
}
