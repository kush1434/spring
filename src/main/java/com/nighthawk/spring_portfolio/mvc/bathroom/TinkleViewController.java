package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

@Controller
public class TinkleViewController {

    @Autowired
    private TinkleJPARepository tinkleRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    @Autowired
    private TinkleStatisticsService tinkleStatisticsService;

    @GetMapping("/admin/tinkle-view")
    public String showTinkleView(
        Model model, 
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes) {
        // Check if user is authenticated
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Get the logged-in user's email
        String email = userDetails.getUsername();
        Person user = personRepository.findByUid(email);

        // Check if the user exists and has ROLE_ADMIN
        if (user == null || !user.hasRoleWithName("ROLE_ADMIN")) {
            return "redirect:/access-denied";
        }
        // Fetch all Tinkle records from the database
        List<Tinkle> tinkleList = tinkleRepository.findAll();

        // Add durations for each entry
        List<Map<String, String>> tinkleDataWithDurations = tinkleList.stream().map(tinkle -> {
            Map<String, String> row = new HashMap<>();
            row.put("person_name", tinkle.getPerson_name());
            row.put("timeIn", tinkle.getTimeIn());
            row.put("duration", tinkleStatisticsService.calculateDurationFormatted(tinkle.getTimeIn()));
            return row;
        }).collect(Collectors.toList());

        // Calculate average weekly durations
        Map<String, String> averageWeeklyDurations = tinkleStatisticsService.calculateAverageWeeklyDurationsFormatted(tinkleList);

        // Prepare data for chart (weekly durations per user)
        Map<String, Long> rawWeeklyDurations = tinkleStatisticsService.calculateAverageWeeklyDurations(tinkleList);
        model.addAttribute("chartLabels", rawWeeklyDurations.keySet()); // User names
        model.addAttribute("chartData", rawWeeklyDurations.values()); // Durations in seconds

        // Pass data for tables
        model.addAttribute("tinkleList", tinkleDataWithDurations); // Updated list with durations
        model.addAttribute("averageWeeklyDurations", averageWeeklyDurations);

        return "tinkle-view"; // Corresponds to the Thymeleaf template
    }
}
