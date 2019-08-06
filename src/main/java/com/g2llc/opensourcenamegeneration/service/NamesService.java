package com.g2llc.opensourcenamegeneration.service;

import com.g2llc.opensourcenamegeneration.dto.NameSpecification;
import com.g2llc.opensourcenamegeneration.error.CustomException;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Log4j
public class NamesService {

    private static final int MAXIMUM_NAMES_PER_HIT = 5;

    @Value("${api.name.generator.baseUrl}")
    private String remoteNameGeneratorBaseUrl;

    @Value("${maximum.names.count}")
    private int maximumNamesCount;

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
        int loop = 1;
        Set<String> names = new HashSet<>();

        do {
            List<String> array = new ArrayList<>();
            log.debug("Connection number " + loop);
            array.addAll(connect(url));


            if (nameSpecification.getCount().intValue() <= MAXIMUM_NAMES_PER_HIT) return array;

            addNames(nameSpecification, names, array);
            loop++;
        } while (isCrawlToContinue(nameSpecification, names));


        return convertSetToList(names);

    }

    private static final List<String> convertSetToList(Set names) {
        List<String> array = new ArrayList<>();
        array.addAll(names);
        return array;
    }

    private List<String> connect(String url) {
        try {
            List<String> array = restTemplate.getForObject(url, ArrayList.class);

            log.debug("===> " + array.toString());
            return array;
        } catch (Error e) {
            log.error("Exception on connection.");
        }
        return null;
    }

    private static final void addNames(NameSpecification nameSpecification, Set names, List<String> array) {
        if (isNumberCompleted(nameSpecification, names) || array == null || array.isEmpty()) {
            return;
        }
        for (String str : array) {
            if (isNumberCompleted(nameSpecification, names)) {
                break;
            }
            names.add(str);
        }
    }


    private static final boolean isCrawlToContinue(NameSpecification nameSpecification, Set names) {
        if (nameSpecification.getCount().intValue() <= MAXIMUM_NAMES_PER_HIT)
            return false;


        return !isNumberCompleted(nameSpecification, names);

    }

    private static final boolean isNumberCompleted(NameSpecification nameSpecification, Set names) {
        return nameSpecification.getCount().intValue() <= names.size();
    }

    private final String buildUrl(NameSpecification nameSpecification) {
        String url = remoteNameGeneratorBaseUrl;
        url = addParameter(url, validateCount(nameSpecification));
        url = addParameter(url, validateType(nameSpecification));
        url = addParameter(url, validateWithSurname(nameSpecification));
        url = addParameter(url, validateFrequency(nameSpecification));
        return url;
    }


    private final String validateCount(NameSpecification nameSpecification) {
        if (nameSpecification == null)
            throw new CustomException("Missing mandatory field count.");

//        if (nameSpecification.getCount().intValue() < 1 || nameSpecification.getCount().intValue() > 5)
//            throw new CustomException("Incorrect value of field count.");

        if (nameSpecification.getCount().intValue() < 1 || nameSpecification.getCount().intValue() > maximumNamesCount)
            throw new CustomException("Incorrect value of field count.");

        if (nameSpecification.getCount().intValue() > MAXIMUM_NAMES_PER_HIT)
            return "count=" + MAXIMUM_NAMES_PER_HIT;

        return "count=" + nameSpecification.getCount().intValue();
    }

    private String addParameter(String url, String parameter) {
        if (StringUtils.isNotBlank(parameter))
            return url.contains("=") ? url + PARAMETER_SEPARATOR + parameter : url + parameter;
        return url;
    }


    private static final String validateType(NameSpecification nameSpecification) {
        if (StringUtils.isBlank(nameSpecification.getType()) || StringUtils.contains(nameSpecification.getType(), "any"))
            return "";

        if (TYPE_PATTERN.matcher(nameSpecification.getType()).find()) {
            return "type=" + nameSpecification.getType();
        }
        throw new CustomException("Incorrect value of field type.");
    }

    private static final String validateWithSurname(NameSpecification nameSpecification) {
        if ((StringUtils.isBlank(nameSpecification.getType()) || !StringUtils.contains(nameSpecification.getType(), "surname")) && nameSpecification.getWithSurname() != null) {
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
