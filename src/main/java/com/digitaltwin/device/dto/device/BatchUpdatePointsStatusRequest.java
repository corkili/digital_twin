package com.digitaltwin.device.dto.device;

import lombok.Data;
import java.util.List;

@Data
public class BatchUpdatePointsStatusRequest {
    private List<Long> pointIds;
    private Boolean published;
}