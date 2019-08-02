package com.g2llc.opensourcenamegeneration.service;

import com.g2llc.opensourcenamegeneration.dto.NameSpecification;
import com.g2llc.opensourcenamegeneration.error.CustomException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NamesService {

    @Value("${api.name.generator.baseUrl}")
    private String remoteNameGeneratorBaseUrl;

    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("common|rare|all");
    private static final Pattern TYPE_PATTERN = Pattern.compile("male|female|surname");
    private static final String PARAMETER_SEPARATOR = "&";

    public List<String> getNames(NameSpecification nameSpecification) {
        return getRandomNames(nameSpecification);
    }

    @Autowired
    RestTemplate restTemplate;

    public List<String> getRandomNames(NameSpecification nameSpecification) {
        String url = buildUrl(nameSpecification);
        System.out.println("===> origin " + remoteNameGeneratorBaseUrl + "count=1&with_surname=true&frequency=common");

        System.out.println("===> " + url);
        try {
            List<String> array = restTemplate.getForObject(url, ArrayList.class);

            System.err.println("===> " + array.toString());
            return array;
        } catch (Error e) {
        }
        return null;
    }

    private final String buildUrl(NameSpecification nameSpecification) {


//        return remoteNameGeneratorBaseUrl + "count=1&with_surname=true&frequency=common";

        String url = remoteNameGeneratorBaseUrl;
        url = addParameter(url, validateCount(nameSpecification));
        url = addParameter(url, validateType(nameSpecification));
        url = addParameter(url, validateWithSurname(nameSpecification));
        url = addParameter(url, validateFrequency(nameSpecification));


        return url;
    }


    private static final String validateCount(NameSpecification nameSpecification) {
        if (nameSpecification == null)
            throw new CustomException("Missing mandatory field count.");

        if (nameSpecification.getCount().intValue() < 1 || nameSpecification.getCount().intValue() > 5)
            throw new CustomException("Incorrect value of field count.");
        return "count=" + nameSpecification.getCount();
    }

    private String addParameter(String url, String parameter) {
        if (StringUtils.isNotBlank(parameter))
            return url + PARAMETER_SEPARATOR + parameter;
        return url;
    }


    private static final String validateType(NameSpecification nameSpecification) {
        if (StringUtils.isBlank(nameSpecification.getType()) || StringUtils.contains(nameSpecification.getType(),"any"))
            return "";

        if (TYPE_PATTERN.matcher(nameSpecification.getType()).find()) {
            return "type=" + nameSpecification.getType();
        }
        throw new CustomException("Incorrect value of field type.");
    }

    private static final String validateWithSurname(NameSpecification nameSpecification) {
        if ((StringUtils.isBlank(nameSpecification.getType()) || ! StringUtils.contains(nameSpecification.getType(), "surname")) && nameSpecification.getWithSurname() != null) {
            return "with_surname=" + nameSpecification.getWithSurname();
        }

        return "";
    }


    private static boolean isWithASurname(NameSpecification nameSpecification) {
        if (nameSpecification.getWithSurname() == null || nameSpecification.getWithSurname().booleanValue() == false)
            return false;

        if (nameSpecification.getWithSurname().booleanValue() == true)
            return true;

        return false;
    }

    private static final String validateFrequency(NameSpecification nameSpecification) {
        if (StringUtils.isBlank(nameSpecification.getFrequency()) && !isValidFrequencyRange(nameSpecification))
            throw new CustomException("Missing or invalid information frequency.");

        String frequency = validateFrequencyRange(nameSpecification);

        System.err.println("here frequency " + frequency);

        if (StringUtils.isBlank(frequency))
            frequency = buildFrequency(nameSpecification);

        if (StringUtils.isNotBlank(frequency))
            return frequency;

        throw new CustomException("Error while generating frequency.");
    }

    private static final String buildFrequency(NameSpecification nameSpecification) {
        if (FREQUENCY_PATTERN.matcher(nameSpecification.getFrequency()).find())
            return "frequency=" + nameSpecification.getFrequency();
        throw new CustomException("Value of frequency incorrect.");
    }

    private static final String validateFrequencyRange(NameSpecification nameSpecification) {
        if (nameSpecification.getMaxFrequency() != null && nameSpecification.getMinFrequency() != null) {
            return "min_freq=" + nameSpecification.getMinFrequency().intValue() + "&max_freq=" + nameSpecification.getMaxFrequency();
        }
        return null;
    }

    private static final boolean isFrequencyRangePresent(NameSpecification nameSpecification) {
        return nameSpecification.getMinFrequency() != null && nameSpecification.getMaxFrequency() != null;
    }

    private static final boolean isValidMinFrequency(NameSpecification nameSpecification) {
        return nameSpecification.getMinFrequency().intValue() > 0 && nameSpecification.getMinFrequency().intValue() < 101;
    }

    private static final boolean isValidMaxFrequency(NameSpecification nameSpecification) {
        return nameSpecification.getMaxFrequency().intValue() > 0 && nameSpecification.getMaxFrequency().intValue() < 101;
    }

    private static final boolean isValidFrequencyRange(NameSpecification nameSpecification) {
        return isFrequencyRangePresent(nameSpecification) && isValidMinFrequency(nameSpecification) && isValidMaxFrequency(nameSpecification)
                && nameSpecification.getMinFrequency().intValue() <= nameSpecification.getMaxFrequency().intValue();
    }


}
