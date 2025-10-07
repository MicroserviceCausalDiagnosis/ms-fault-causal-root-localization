package com.example.causalanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Span {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private String operationName;
    private long startTime;
    private long duration; // 耗时(毫秒)
    private boolean isError;
    private String component;
    private List<Tag> tags;

    // 构造方法、getter、setter
}