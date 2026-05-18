package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.service.DonationProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DonationProgramService donationProgramService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("programs", donationProgramService.findAllActive());
        return "index";
    }
}
