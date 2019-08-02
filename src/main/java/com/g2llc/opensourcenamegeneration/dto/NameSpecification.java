package com.g2llc.opensourcenamegeneration.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameSpecification {

    private String count;
    private Boolean withSurname;
    private String frequency;
    private String type;
    private Integer minFrequency;
    private Integer maxFrequency;

}
