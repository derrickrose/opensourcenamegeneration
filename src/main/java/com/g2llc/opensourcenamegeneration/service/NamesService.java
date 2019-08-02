package com.g2llc.opensourcenamegeneration.service;

import com.g2llc.opensourcenamegeneration.dto.NameSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class NamesService {

    @Value("${api.name.generator.baseUrl}")
    private String remoteNameGeneratorBaseUrl;


    public List<String> getNames(NameSpecification nameSpecification) {
        return getRandomNames(nameSpecification);
    }


    @Autowired
    RestTemplate restTemplate;

    public List<String> getRandomNames(NameSpecification nameSpecification) {

        String url = buildUrl(nameSpecification);
        System.out.println("===> " + url);
        try {
            List<String> array = restTemplate.getForObject(url, ArrayList.class);
            return array;
        } catch (Error e) {

            // throw new InternalException("UnosquareError on attempt to list daily rates.");
        }
        return null;

    }

    private final String buildUrl(NameSpecification nameSpecification) {
        // return remoteNameGeneratorBaseUrl
        return remoteNameGeneratorBaseUrl+"count=1&with_surname=true&frequency=common";
    }


}
