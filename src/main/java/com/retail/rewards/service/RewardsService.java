package com.retail.rewards.service;

import com.retail.rewards.model.Customer;
import com.retail.rewards.model.MonthlyRewards;
import com.retail.rewards.model.TransactionRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RewardsService {

    private final SampleDataLoader loader;

    public RewardsService(SampleDataLoader loader) {
        this.loader = loader;
    }

    // Build a list of Customer DTOs from the loaded sample data.
    // Notes:
    // - We keep the mapping simple: each top-level customer key maps to months -> transactions.
    // - Defensive checks are used so malformed or missing data won't blow up the service.
    public List<Customer> getCustomerRewards() {
        List<Customer> customers = new ArrayList<>();
        // Get sample data for 3 customers for 3 months
        Map<String, Map<Integer, List<Double>>> customerData = loader.getData();
        if (customerData == null || customerData.isEmpty()) {
            return customers;
        }

        customerData.forEach((customerName, transactionsByMonth) -> {
            if (customerName == null || transactionsByMonth == null) return;
            List<MonthlyRewards> monthlyRewards = new ArrayList<>();
            int totalRewards = 0;
            for (Map.Entry<Integer, List<Double>> entry : transactionsByMonth.entrySet()) {
                Integer month = entry.getKey();
                List<Double> transactions = entry.getValue();
                int rewardForMonth = calculateRewards(transactions);
                // If month is missing, normalize to 0 so the DTO is consistent
                monthlyRewards.add(new MonthlyRewards(month == null ? 0 : month, rewardForMonth));
                totalRewards += rewardForMonth;
            }

            customers.add(new Customer(customerName, monthlyRewards, totalRewards));
        });

        return customers;
    }

    // New API: flatten all sample-data transactions to a list of TransactionRecord DTOs.
    // This is useful for debugging or returning raw transaction lists to clients.
    public List<TransactionRecord> getAllTransactions() {
        List<TransactionRecord> out = new ArrayList<>();
        Map<String, Map<Integer, List<Double>>> customerData = loader.getData();
        if (customerData == null || customerData.isEmpty()) return out;

        customerData.forEach((customerName, transactionsByMonth) -> {
            if (customerName == null || transactionsByMonth == null) return;
            for (Map.Entry<Integer, List<Double>> entry : transactionsByMonth.entrySet()) {
                Integer month = entry.getKey();
                List<Double> transactions = entry.getValue();
                if (transactions == null) continue;
                int safeMonth = month == null ? 0 : month;
                for (Double t : transactions) {
                    if (t == null) continue;
                    // Skip non-positive amounts, keep only valid transactions
                    if (t <= 0) continue;
                    out.add(new TransactionRecord(customerName, safeMonth, t));
                }
            }
        });
        return out;
    }

    // Lookup by customer name (case-insensitive). Return Optional.empty() for null/blank input.
    // This keeps the controller code simple and centralizes lookup behavior here.
    public Optional<Customer> getCustomerRewardsByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return getCustomerRewards().stream()
                .filter(c -> name.equalsIgnoreCase(c.customerName()))
                .findFirst();
    }

    // Calculate rewards for a list of transaction amounts.
    // Important business decisions encoded here:
    // - We count only whole dollars toward points (Math.floor), because the program awards points "per dollar".
    // - Transactions <= 0 or null are ignored.
    // - For amounts > 100: award 2 points per dollar over 100, plus the 50 points for the $50..$100 band.
    // - For amounts > 50 and <= 100: award 1 point per dollar over 50.
    // Rationale: Doing the floor() arithmetic here makes reasoning about totals deterministic and easy to test.
    private int calculateRewards(List<Double> transactions) {
        if (transactions == null || transactions.isEmpty()) return 0;
        int rewards = 0;
        for (Double t : transactions) {
            if (t == null || t <= 0) continue; // ignore invalid or non-positive amounts
            double transaction = t;
            // points for every dollar over 100 => 2 points per dollar
            if (transaction > 100) {
                // Only full dollars count. e.g., 120.99 => floor(20.99) -> 20 -> 2*20
                rewards += (int) (2 * Math.floor(transaction - 100));
                // plus 1 point for each dollar between 50 and 100 => 50 points
                // (we award the whole 50-dollar band when transaction exceeds 100)
                rewards += 50;
            } else if (transaction > 50) {
                // For transactions between 50 and 100, count whole dollars over 50.
                rewards += (int) Math.floor(transaction - 50);
            }
        }
        return rewards;
    }
}
