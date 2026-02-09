package com.retail.rewards;

import com.retail.rewards.controller.RewardsController;
import com.retail.rewards.service.RewardsService;
import com.retail.rewards.service.SampleDataLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RewardsApplicationTests {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private SampleDataLoader sampleDataLoader;

    @Autowired
    private RewardsController rewardsController;

    @Test
    void contextLoads() {
        // Basic smoke test - context started if we reach here
        assertThat(ctx).isNotNull();
    }

    @Test
    void beansArePresent() {
        assertThat(rewardsService).as("rewardsService bean").isNotNull();
        assertThat(sampleDataLoader).as("sampleDataLoader bean").isNotNull();
        assertThat(rewardsController).as("rewardsController bean").isNotNull();
    }

    @Test
    void sampleDataIsLoaded() {
        Map<String, Map<Integer, java.util.List<Double>>> data = sampleDataLoader.getData();
        assertThat(data).as("sample data map").isNotNull();
        // Expect at least one known key from the sample file
        assertThat(data.keySet()).as("contains expected keys").containsAnyOf("Customer1", "EdgeCases", "BigSpender");
    }

}
