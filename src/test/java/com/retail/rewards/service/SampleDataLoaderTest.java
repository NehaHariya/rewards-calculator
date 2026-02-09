package com.retail.rewards.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SampleDataLoaderTest {

    @Test
    void loadsSampleDataFromResources() {
        SampleDataLoader loader = new SampleDataLoader(new ObjectMapper());
        Map<String, Map<Integer, java.util.List<Double>>> data = loader.getData();
        assertNotNull(data);
        // Should include Customer1 and EdgeCases keys added earlier
        assertTrue(data.containsKey("Customer1"));
        assertTrue(data.containsKey("EdgeCases"));
    }
}
