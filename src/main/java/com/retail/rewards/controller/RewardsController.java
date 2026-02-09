package com.retail.rewards.controller;

import com.retail.rewards.model.Customer;
import com.retail.rewards.model.TransactionRecord;
import com.retail.rewards.service.RewardsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = {"/api/rewards", "/"})
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    // Returns all customers and their computed rewards.
    // This is a simple read-only endpoint backed by the in-memory sample data loader.
    @GetMapping(value = "/customers")
    public ResponseEntity<List<Customer>> getAllCustomerRewards() {
        List<Customer> customers = rewardsService.getCustomerRewards();
        return ResponseEntity.ok(customers);
    }

    // Return a single customer by name. We validate the path variable and return
    // 400 for blank names and 404 when a customer isn't found.
    @GetMapping(value = "/customers/{name}")
    public ResponseEntity<Customer> getCustomerRewardsByName(@PathVariable String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return rewardsService.getCustomerRewardsByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // New endpoint: return a flat list of all valid transactions from the sample data
    @GetMapping(value = "/transactions")
    public ResponseEntity<List<TransactionRecord>> getAllTransactions() {
        List<TransactionRecord> tx = rewardsService.getAllTransactions();
        return ResponseEntity.ok(tx);
    }
}
