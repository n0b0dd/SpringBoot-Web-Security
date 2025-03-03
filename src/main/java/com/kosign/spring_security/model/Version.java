package com.kosign.spring_security.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Version {
    private int major;
    private int minor;
    private int patch;
    private String suffix;
}