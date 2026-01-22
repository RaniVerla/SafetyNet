package net.example.safetynet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequestMapping("/safetynet")
@RestController
public class TestController {
    
    @GetMapping("/test")
    public HttpStatus testReturn()
    {
        log.info("Testing Person Get method");
        return HttpStatus.OK;
    }
    
    
}
