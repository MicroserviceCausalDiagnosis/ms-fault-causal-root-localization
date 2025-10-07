package com.example.causalanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Trace {
    private String traceId;
    private List<Span> spans;
    private long startTime;
    private long endTime;
    private boolean hasError;

    // 构造方法、getter、setter
}