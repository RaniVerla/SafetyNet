package net.example.safetynet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FireAlertResponse {

    private int stationNumber;
    private List<FireAlertResident> residents;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FireAlertResident {
        private String firstName;
        private String lastName;
        private String phone;
        private int age;
        private List<String> medications;
        private List<String> allergies;
    }
}
