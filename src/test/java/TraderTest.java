import com.model.StockData;
import com.strategy.BuyAndHoldEvenly;
import com.strategy.RandomStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.strategy.Strategy;
import com.model.Trader;
import com.model.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class TraderTest {

    private Trader trader;
    private Stock stock1;
    private Stock stock2;
    private static final String STARTDATE = "2000-01-01";

    @BeforeEach
    void setUp() {
        // Assuming you have a mock or simple implementation for Strategy and Stock
        Strategy mockStrategy = new BuyAndHoldEvenly();
        trader = new Trader(mockStrategy, 1000); // Initial cash: 1000

        // Mock Stocks (or create a simple stock class)
        stock1 = new Stock("AAPL", new ArrayList<>(List.of(new StockData(LocalDate.parse(STARTDATE), 150)))); // Stock AAPL, price 150
        stock2 = new Stock("GOOG", new ArrayList<>(List.of(new StockData(LocalDate.parse(STARTDATE), 200)))); // Stock GOOG, price 200

    }

    @Test
    void testTraderInitialization() {
        assertNotNull(trader);
        assertEquals(1000, trader.getCash());
        assertEquals(0, trader.getPortfolio().size());
    }

    @Test
    void testMakeDecisionAndBuyStock() {
        // Assume MockStrategy buys AAPL
        trader.makeDecision(List.of(stock1, stock2));

        assertEquals(3, trader.getPortfolio().get("AAPL"));  // 500 / 150 = 3 shares
        assertEquals(2, trader.getPortfolio().get("GOOG")); // 500/200 = 2 shares
        assertEquals(1000 - (850), trader.getCash()); // Cash should be reduced by 900 (6 * 150)
    }

    @Test
    void testInsufficientFundsToBuy() {
        trader.makeDecision(List.of(stock1, stock2));
        // Cash remaining should not go below 0 after an attempt to buy
        trader.makeDecision(List.of(stock1)); // Another decision should fail because the cash is low
        assertTrue(trader.getCash() >= 0); // Ensure no negative balance
    }

    @Test
    void testTransactionHistory() {
        trader.makeDecision(List.of(stock1));
        List<String> history = trader.getTransactionsHistory();

        assertEquals(1, history.size());
        assertTrue(history.get(0).contains("BUY"));
        assertTrue(history.get(0).contains("AAPL"));
        assertTrue(history.get(0).contains("$150"));
    }

    @Test
    void testStockTotalValue() {
        trader.makeDecision(List.of(stock1)); // Buying AAPL stock
        double totalValue = trader.getStockTotalValue(stock1);

        assertEquals(6 * 150, totalValue); // Total value of AAPL stock should be 6 * 150 = 900
    }

    // You can add more tests as needed
}
