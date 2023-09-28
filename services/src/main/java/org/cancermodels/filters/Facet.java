package org.cancermodels.filters;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Facet {
    private String name;
    private Set<String> values;
}

