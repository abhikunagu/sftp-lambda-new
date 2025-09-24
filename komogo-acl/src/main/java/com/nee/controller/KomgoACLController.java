package com.nee.controller;


import com.nee.service.CollatralService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@RestController
@EnableWebMvc
@RequestMapping("/api")
public class KomgoACLController {

    private final CollatralService collatralService;

    public KomgoACLController(CollatralService collatralService) {
        this.collatralService = collatralService;
    }

    @PostMapping("/post")
    public ResponseEntity<?> pushEvent() {
        return collatralService.pushEvent();
    }
}
