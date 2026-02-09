package com.retail.rewards.model;

/**
 * Simple DTO representing a single transaction: customer name, month and amount.
 */
public record TransactionRecord(String customerName, int month, double amount) {
}
