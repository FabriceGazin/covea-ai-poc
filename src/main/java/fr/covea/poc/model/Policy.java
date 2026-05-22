package fr.covea.poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
    private int id;
    private String holderId;
    private String type;
    private String[] coverages;
    private LocalDate startDate;
    private LocalDate endDate;
    private double deductible;
    private double maxCoverage;
    private Status status;

    public enum Status {
        ACTIVE, SUSPENDED, EXPIRED
    }
}