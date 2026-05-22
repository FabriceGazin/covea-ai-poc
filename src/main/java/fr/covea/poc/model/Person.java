package fr.covea.poc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    int id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String gender;
    String dateOfBirth;
    List<Address> addresses;
}
