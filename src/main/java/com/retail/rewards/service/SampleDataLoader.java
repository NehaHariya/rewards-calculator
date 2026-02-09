package com.retail.rewards.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SampleDataLoader {

    private final Map<String, Map<Integer, List<Double>>> data;

    // Load sample-data.json from classpath at construction time.
    // We intentionally keep the loader lightweight:
    // - Use Jackson to parse into a nested Map structure.
    // - If parsing fails or the resource is missing, fall back to an empty map so the app still runs.
    public SampleDataLoader(ObjectMapper objectMapper) {
        Map<String, Map<Integer, List<Double>>> parsed = Collections.emptyMap();
        try {
            ClassPathResource resource = new ClassPathResource("sample-data.json");
            parsed = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            // If sample data can't be read, keep an empty dataset — app should still run
            parsed = Collections.emptyMap();
        }
        this.data = Collections.unmodifiableMap(parsed);
    }

    // Return an immutable view of the parsed data. Consumers should defensively handle missing keys.
    public Map<String, Map<Integer, List<Double>>> getData() {
        return data;
    }
}
