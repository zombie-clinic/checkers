package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {

    @GetMapping("/")
    public String index(Model model) {
        boolean[][] myArray = {
                {true, false, true},
                {false, true, false},
                {true, false, true}
        };
        model.addAttribute("myArray", myArray);
        return "index";
    }
}
