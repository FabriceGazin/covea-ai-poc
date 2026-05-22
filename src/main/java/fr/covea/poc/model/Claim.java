package fr.covea.poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Claim {
    private int id;
    private String policyId;
    private int claimantId;
    private String type;
    private String status;
    private String description;
    private String incidentDate;
    private LocalDate reportedAt;
    private String location;
    private double estimatedAmount;
    private double approvedAmount;
    private String notes;
}