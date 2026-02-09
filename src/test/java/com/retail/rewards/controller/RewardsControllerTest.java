package com.retail.rewards.controller;

import com.retail.rewards.model.Customer;
import com.retail.rewards.model.MonthlyRewards;
import com.retail.rewards.model.TransactionRecord;
import com.retail.rewards.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    private List<Customer> customers;

    @BeforeEach
    void setUp() {
        customers = List.of(new Customer("C1", List.of(new MonthlyRewards(1, 10)), 10));
        when(rewardsService.getCustomerRewards()).thenReturn(customers);
        Mockito.when(rewardsService.getCustomerRewardsByName(anyString())).thenAnswer(inv -> {
            String name = inv.getArgument(0);
            return customers.stream().filter(c -> c.customerName().equalsIgnoreCase(name)).findFirst();
        });

        // Add sample transactions for the transactions endpoint tests
        List<TransactionRecord> tx = Arrays.asList(
                new TransactionRecord("C1", 1, 10.0),
                new TransactionRecord("C2", 2, 20.5)
        );
        when(rewardsService.getAllTransactions()).thenReturn(tx);
    }

    @Test
    void getAllCustomersReturnsOk() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getSpecificCustomerReturnsOk() throws Exception {
        mockMvc.perform(get("/customers/C1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getSpecificCustomerBadRequest() throws Exception {
        mockMvc.perform(get("/customers/ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSpecificCustomerNotFound() throws Exception {
        mockMvc.perform(get("/customers/NoSuch"))
                .andExpect(status().isNotFound());
    }

    // New tests for the transactions endpoint (merged into this class)
    @Test
    void transactionsEndpointReturnsJsonArray() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void apiTransactionsEndpointAlsoWorks() throws Exception {
        mockMvc.perform(get("/api/rewards/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}
