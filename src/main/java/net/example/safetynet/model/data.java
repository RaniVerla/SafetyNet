package net.example.safetynet.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class data {

    private Person persons;
    private Firestation firestations;
    private Medicalrecord medicalrecords;
}
