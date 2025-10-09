package com.playvox.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class PlayvoxRecord {

    private String playvoxId;
    private BigInteger intervalId;
}
