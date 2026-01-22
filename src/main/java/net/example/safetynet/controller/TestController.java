package net.example.safetynet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/safetynet")
@RestController
public class TestController {
    
    @GetMapping("/test")
    public HttpStatus testReturn()
    {
        return HttpStatus.OK;
    }
    
    
}
