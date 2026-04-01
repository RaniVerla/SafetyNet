package net.example.safetynet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FireStationResidents {

    private List<ResidentInfo> residents;
    private int adultCount;
    private int childCount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentInfo {
        private String firstName;
        private String lastName;
        private String address;
        private String phone;
    }
}


