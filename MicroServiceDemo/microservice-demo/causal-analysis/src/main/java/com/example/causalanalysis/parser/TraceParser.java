package com.example.causalanalysis.parser;

import com.example.causalanalysis.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TraceParser {

    public Trace parseTrace(JsonNode traceData) {
        Trace trace = new Trace();
        List<Span> spans = new ArrayList<>();

        JsonNode spansNode = traceData.path("data").path("trace").path("spans");

        for (JsonNode spanNode : spansNode) {
            Span span = parseSpan(spanNode);
            spans.add(span);
        }

        trace.setSpans(spans);
        trace.setTraceId(spans.get(0).getTraceId()); // 取第一个span的traceId
        trace.setStartTime(calculateTraceStartTime(spans));
        trace.setEndTime(calculateTraceEndTime(spans));
        trace.setHasError(checkTraceHasError(spans));

        return trace;
    }

    private Span parseSpan(JsonNode spanNode) {
        Span span = new Span();

        span.setSpanId(spanNode.path("spanId").asText());
        span.setParentSpanId(spanNode.path("parentSpanId").asText());
        span.setServiceName(spanNode.path("serviceCode").asText());
        span.setOperationName(spanNode.path("endpointName").asText());

        long startTime = spanNode.path("startTime").asLong();
        long endTime = spanNode.path("endTime").asLong();
        span.setStartTime(startTime);
        span.setDuration(endTime - startTime);

        span.setError(spanNode.path("isError").asBoolean());
        span.setComponent(spanNode.path("component").asText());

        // 解析tags
        List<Tag> tags = parseTags(spanNode.path("tags"));
        span.setTags(tags);

        return span;
    }

    private List<Tag> parseTags(JsonNode tagsNode) {
        List<Tag> tags = new ArrayList<>();
        for (JsonNode tagNode : tagsNode) {
            Tag tag = new Tag();
            tag.setKey(tagNode.path("key").asText());
            tag.setValue(tagNode.path("value").asText());
            tags.add(tag);
        }
        return tags;
    }

    private long calculateTraceStartTime(List<Span> spans) {
        return spans.stream()
                .mapToLong(Span::getStartTime)
                .min()
                .orElse(0L);
    }

    private long calculateTraceEndTime(List<Span> spans) {
        return spans.stream()
                .mapToLong(span -> span.getStartTime() + span.getDuration())
                .max()
                .orElse(0L);
    }

    private boolean checkTraceHasError(List<Span> spans) {
        return spans.stream().anyMatch(Span::isError);
    }
}