package com.example.perf.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class WorkController {

    // Intentional inefficient implementation to act as a bottleneck later
    @GetMapping("/work")
    public String doWork(@RequestParam(defaultValue = "10000") int n) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // quadratic behaviour: repeatedly scanning the list
            if (!list.contains(i)) {
                list.add(i);
            }
        }
        return "Processed " + list.size() + " items";
    }
}
