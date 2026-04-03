package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import net.example.safetynet.model.ChildAlertResponse;
import net.example.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ChildAlertController {

    @Autowired
    private PersonService personService;

    /**
     * Get children at a specific address
     *
     * @param address the address to query
     * @return ChildAlertResponse containing list of children with their info and household members
     */
    @GetMapping("/childAlert")
    public ChildAlertResponse getChildrenAtAddress(@RequestParam String address) {
        // Remove surrounding quotes if present
        String cleanAddress = address;
        if (cleanAddress != null && cleanAddress.startsWith("\"") && cleanAddress.endsWith("\"")) {
            cleanAddress = cleanAddress.substring(1, cleanAddress.length() - 1);
        }
        log.info("Fetching children at address: {}", cleanAddress);
        ChildAlertResponse response = personService.getChildrenAtAddress(cleanAddress);
        if (response.getChildren().isEmpty()) {
            // Return empty string if no children
            return new ChildAlertResponse(new java.util.ArrayList<>());
        }
        return response;
    }
}
