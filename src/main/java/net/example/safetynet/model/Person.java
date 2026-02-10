package net.example.safetynet.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zip;
    private String phone;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(firstName, person.firstName)
                && Objects.equals(lastName, person.lastName)
                && Objects.equals(address, person.address)
                && Objects.equals(city, person.city)
                && Objects.equals(zip, person.zip)
                && Objects.equals(phone, person.phone)
                && Objects.equals(email, person.email);
    }


    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, address, city, zip, phone, email);
    }
}
