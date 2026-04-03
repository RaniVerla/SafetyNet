package net.example.safetynet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChildAlertResponse {

    private List<ChildInfo> children;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildInfo {
        private String firstName;
        private String lastName;
        private int age;
        private List<HouseholdMember> householdMembers;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HouseholdMember {
        private String firstName;
        private String lastName;
    }
}
