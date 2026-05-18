package kz.testmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }

    @GetMapping("/tests")
    public String testsPage() {
        return "redirect:/tests.html";
    }

    @GetMapping("/tests/{id}/session")
    public String sessionPage() {
        return "redirect:/session.html";
    }

    @GetMapping("/tests/{id}/statistics")
    public String statisticsPage() {
        return "redirect:/statistics.html";
    }
}
