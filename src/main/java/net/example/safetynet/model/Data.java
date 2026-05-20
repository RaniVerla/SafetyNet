package net.example.safetynet.model;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data {

    private List<Person> persons;
    private List<Firestation> firestations;
    private List<Medicalrecord> medicalrecords;
}
