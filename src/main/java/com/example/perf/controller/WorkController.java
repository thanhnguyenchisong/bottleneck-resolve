package com.example.perf.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WorkController {

    // Intentional inefficient implementation to act as a bottleneck later
    @GetMapping("/work")
    public String doWork(@RequestParam(defaultValue = "10000") int n) {
//        List<Integer> list = new ArrayList<>();
        Map<Integer, Boolean> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            // quadratic behaviour: repeatedly scanning the list
//            if (!list.contains(i)) {
//                list.add(i);
//            }
            //this is improved code using a map to avoid the O(n) contains check
            map.putIfAbsent(i, true);
        }
        return "Processed " + map.size() + " items";
    }
}
