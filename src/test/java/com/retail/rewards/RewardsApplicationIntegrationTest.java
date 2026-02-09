package com.retail.rewards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RewardsApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void customersEndpointReturnsList() {
        String url = "http://localhost:" + port + "/customers";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("Customer1");
    }

    @Test
    void apiRewardsCustomersEndpointReturnsList() {
        String url = "http://localhost:" + port + "/api/rewards/customers";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("Customer2");
    }
}
