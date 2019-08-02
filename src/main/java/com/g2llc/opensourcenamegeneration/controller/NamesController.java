package com.g2llc.opensourcenamegeneration.controller;

import com.g2llc.opensourcenamegeneration.dto.NameSpecification;
import com.g2llc.opensourcenamegeneration.service.NamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NamesController {

    @Autowired
    private NamesService namesService;


    @GetMapping("/test")
    public List<String> getNames(NameSpecification nameSpecification) {
        System.err.println("hahahahahhhahhhhhhhhhhahahah");
        return namesService.getNames(nameSpecification);
    }



}
