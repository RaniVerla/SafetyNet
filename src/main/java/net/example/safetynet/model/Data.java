package net.example.safetynet.model;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data {

    private Person persons;
    private Firestation firestations;
    private Medicalrecord medicalrecords;
}
