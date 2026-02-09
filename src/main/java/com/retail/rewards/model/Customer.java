package com.retail.rewards.model;

import java.util.List;

public record Customer(String customerName, List<MonthlyRewards> monthlyRewards, int totalRewards) {
}
