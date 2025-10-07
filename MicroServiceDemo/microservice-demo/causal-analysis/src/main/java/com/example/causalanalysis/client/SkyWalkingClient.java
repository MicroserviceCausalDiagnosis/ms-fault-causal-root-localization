package com.example.causalanalysis.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SkyWalkingClient {
    private final String SKYWALKING_GRAPHQL_URL = "http://localhost:12800/graphql";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode queryTrace(String traceId) {
        try {
            String graphqlQuery = String.format("""
            {
              trace: queryTrace(traceId: "%s") {
                spans {
                  traceId
                  segmentId
                  spanId
                  parentSpanId
                  serviceCode
                  serviceInstanceName
                  startTime
                  endTime
                  endpointName
                  type
                  peer
                  component
                  isError
                  layer
                  tags {
                    key
                    value
                  }
                }
              }
            }
            """, traceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format("""
            {
              "query": "%s"
            }
            """, graphqlQuery.replace("\"", "\\\"").replace("\n", ""));

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    SKYWALKING_GRAPHQL_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用SkyWalking API失败: " + e.getMessage(), e);
        }
    }
}