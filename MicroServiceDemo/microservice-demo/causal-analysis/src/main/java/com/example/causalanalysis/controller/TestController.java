package com.example.causalanalysis.controller;

import com.example.causalanalysis.client.SkyWalkingClient;
import com.example.causalanalysis.model.Trace;
import com.example.causalanalysis.parser.TraceParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trace")
public class TestController {

    @Autowired
    private SkyWalkingClient skyWalkingClient;

    @Autowired
    private TraceParser traceParser;

    @GetMapping("/{traceId}")
    public Trace getTrace(@PathVariable String traceId) {
        // 1. 从SkyWalking获取原始数据
        JsonNode rawData = skyWalkingClient.queryTrace(traceId);

        // 2. 解析为简化模型
        Trace trace = traceParser.parseTrace(rawData);

        // 3. 打印调试信息
        System.out.println("=== 解析结果 ===");
        System.out.println("Trace ID: " + trace.getTraceId());
        System.out.println("Span数量: " + trace.getSpans().size());
        System.out.println("总耗时: " + (trace.getEndTime() - trace.getStartTime()) + "ms");
        System.out.println("包含错误: " + trace.isHasError());

        trace.getSpans().forEach(span -> {
            System.out.println(" - " + span.getServiceName() + " : " +
                    span.getOperationName() + " : " +
                    span.getDuration() + "ms");
        });

        return trace;
    }
}