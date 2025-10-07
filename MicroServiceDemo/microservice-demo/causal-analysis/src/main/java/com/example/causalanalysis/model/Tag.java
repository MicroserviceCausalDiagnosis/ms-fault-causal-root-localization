package com.example.causalanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Tag {
    private String key;
    private String value;

    // 构造方法、getter、setter
}