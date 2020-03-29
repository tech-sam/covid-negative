package com.covid19negative.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String welcomeMsg() {
        return "Lets come together and fight against Novel Corona Virus ";
    }
}
