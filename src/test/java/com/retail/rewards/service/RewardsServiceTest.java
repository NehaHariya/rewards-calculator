package com.retail.rewards.service;

import com.retail.rewards.model.Customer;
import com.retail.rewards.model.MonthlyRewards;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class RewardsServiceTest {

    // Helper subclass to override getData for controlled test inputs
    static class TestSampleDataLoader extends SampleDataLoader {
        private final Map<String, Map<Integer, List<Double>>> custom;

        public TestSampleDataLoader(Map<String, Map<Integer, List<Double>>> custom) {
            super(new ObjectMapper()); // call parent ctor, but we'll override getData()
            this.custom = custom;
        }

        @Override
        public Map<String, Map<Integer, List<Double>>> getData() {
            return custom;
        }
    }

    @Test
    void singleTransactionOver100YieldsCorrectPoints() {
        Map<Integer, List<Double>> months = new HashMap<>();
        months.put(1, List.of(120.0)); // expected 90 points
        Map<String, Map<Integer, List<Double>>> data = new HashMap<>();
        data.put("T1", months);

        RewardsService svc = new RewardsService(new TestSampleDataLoader(data));
        List<Customer> customers = svc.getCustomerRewards();
        assertEquals(1, customers.size());
        Customer c = customers.get(0);
        assertEquals("T1", c.customerName());
        assertEquals(1, c.monthlyRewards().size());
        MonthlyRewards mr = c.monthlyRewards().get(0);
        assertEquals(1, mr.month());
        assertEquals(90, mr.rewards());
        assertEquals(90, c.totalRewards());
    }

    @Test
    void thresholdsAndFractionalDollars() {
        Map<Integer, List<Double>> months = new HashMap<>();
        months.put(1, Arrays.asList(50.0, 50.99, 100.0, 100.01));
        Map<String, Map<Integer, List<Double>>> data = new HashMap<>();
        data.put("Edge", months);

        RewardsService svc = new RewardsService(new TestSampleDataLoader(data));
        List<Customer> customers = svc.getCustomerRewards();
        Customer c = customers.get(0);

        MonthlyRewards mr = c.monthlyRewards().get(0);
        // Calculation per transaction (using floor semantics):
        // 50.0 -> 0
        // 50.99 -> floor(0.99)=0 -> 0
        // 100.0 -> 50
        // 100.01 -> floor(0.01)=0 -> +50 (over-100 branch adds 50)
        // total = 100
        assertEquals(100, mr.rewards());
        assertEquals(100, c.totalRewards());
    }

    @Test
    void negativeNullAndZeroIgnored() {
        Map<Integer, List<Double>> months = new HashMap<>();
        months.put(1, Arrays.asList(0.0, -5.0, null));
        Map<String, Map<Integer, List<Double>>> data = new HashMap<>();
        data.put("Bad", months);

        RewardsService svc = new RewardsService(new TestSampleDataLoader(data));
        List<Customer> customers = svc.getCustomerRewards();
        Customer c = customers.get(0);
        MonthlyRewards mr = c.monthlyRewards().get(0);
        assertEquals(0, mr.rewards());
        assertEquals(0, c.totalRewards());
    }

    @Test
    void multipleMonthsAndTotals() {
        Map<Integer, List<Double>> months = new HashMap<>();
        months.put(1, List.of(55.0)); // 5 points
        months.put(2, List.of(120.0)); // 90 points
        months.put(3, List.of(200.0)); // over 100: floor 100 -> 2*100=200 +50 =250
        Map<String, Map<Integer, List<Double>>> data = new HashMap<>();
        data.put("M", months);

        RewardsService svc = new RewardsService(new TestSampleDataLoader(data));
        Customer c = svc.getCustomerRewards().get(0);
        assertEquals(3, c.monthlyRewards().size());
        int total = c.totalRewards();
        assertEquals(5 + 90 + 250, total);
    }

    // New tests for the transactions flattening API
    @Test
    void getAllTransactions_filtersInvalidAndPreservesValid() {
        Map<Integer, List<Double>> months = new HashMap<>();
        months.put(1, Arrays.asList(10.0, 0.0, -2.0, null));
        // include a null month key to ensure normalization to 0
        months.put(null, List.of(20.0));

        Map<String, Map<Integer, List<Double>>> data = new HashMap<>();
        data.put("C1", months);

        RewardsService svc = new RewardsService(new TestSampleDataLoader(data));
        var tx = svc.getAllTransactions();

        // Expect only the positive, non-null transactions: 10.0 and 20.0
        assertEquals(2, tx.size());

        boolean found10 = false;
        boolean found20 = false;
        for (var r : tx) {
            if ("C1".equals(r.customerName()) && r.month() == 1 && Double.compare(r.amount(), 10.0) == 0) found10 = true;
            if ("C1".equals(r.customerName()) && r.month() == 0 && Double.compare(r.amount(), 20.0) == 0) found20 = true;
        }

        assertTrue(found10, "should contain 10.0 transaction for month 1");
        assertTrue(found20, "should contain 20.0 transaction for normalized month 0");
    }

    @Test
    void emptyOrMissingDataProducesEmptyList() {
        Map<String, Map<Integer, List<Double>>> empty = Collections.emptyMap();
        RewardsService svc = new RewardsService(new TestSampleDataLoader(empty));
        var tx = svc.getAllTransactions();
        assertNotNull(tx);
        assertTrue(tx.isEmpty());
    }
}
