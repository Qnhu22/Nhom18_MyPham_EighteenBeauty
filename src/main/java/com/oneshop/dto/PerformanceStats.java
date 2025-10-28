package com.oneshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceStats {
    private long delivered;
    private long cancelled;
    private long returned;
}
