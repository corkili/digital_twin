package com.digitaltwin.device.dto;

import com.digitaltwin.device.dto.device.PointDto;
import lombok.Data;
import java.util.List;

@Data
public class GroupDto {
    private Long id;
    private String name;
    private String description;
    private List<PointDto> points;
}