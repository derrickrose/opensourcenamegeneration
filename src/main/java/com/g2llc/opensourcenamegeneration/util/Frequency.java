package com.g2llc.opensourcenamegeneration.util;

public enum Frequency {

    RARE("rare"),
    COMMON("common");

    private Frequency(String frequency) {
        this.frequency = frequency;
    }

    private String frequency;
}
