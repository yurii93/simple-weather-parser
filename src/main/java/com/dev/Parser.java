package com.dev;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {

    private final static String DEFAULT_KIEV_PARSE_URL = "http://www.nepogoda.ru/europe/ukrain/kiev/";
    private final String parseUrl;
    private final Pattern datePattern = Pattern.compile("\\d{2}\\.\\d{2}");
    private final Map<String, Integer> outputFormatScheme = new LinkedHashMap<>();
    private static List<List<String>> weatherDataByDate = new ArrayList<>();

    {
        outputFormatScheme.put("ДАТА", 10);
        outputFormatScheme.put("ЧАСТЬ СУТОК", 15);
        outputFormatScheme.put("ПОГОДНЫЕ ЯВЛЕНИЯ", 80);
        outputFormatScheme.put("ТЕМПЕРАТУРА", 15);
        outputFormatScheme.put("ДАВЛЕНИЕ", 12);
        outputFormatScheme.put("ВЛАЖНОСТЬ", 12);
        outputFormatScheme.put("ВЕТЕР", 12);
    }

    public static void main(String[] args) throws NoSuchElementException, IOException {
        Parser parser = new Parser(DEFAULT_KIEV_PARSE_URL);
        parser.initParsing();
        parser.printData();
    }

    private Parser(String parseUrl) {
        this.parseUrl = parseUrl;
    }

    private void initParsing() throws IOException {
        parsePage(getPage());
    }

    private void printData() {
        printTitle();
        printWeatherData();
    }

    private void parsePage(Document page) {
        Element table = page.select("table[class=wt]").first();
        Elements rows = table.select("tr");

        String currentDate = "";

        for (Element row : rows) {
            if (row.hasClass("wth")) {
                String dateString = row.select("th[id=dt]").text();
                currentDate = getDateFromString(dateString);
            } else if (row.hasAttr("valign")) {
                weatherDataByDate.add(collectWeatherDataByDate(row, currentDate));
            }
        }
    }

    private List<String> collectWeatherDataByDate(Element row, String date) {
        return Stream.concat(
                Stream.of(date),
                row.select("td").stream().map(Element::text)
        ).collect(Collectors.toList());
    }

    private void printTitle() {
        if (!weatherDataByDate.isEmpty()) {
            outputFormatScheme.forEach((key, value) -> System.out.printf("%-" + value + "s", key));
            System.out.println();
        } else {
            System.out.println("Нет данных!");
        }
    }

    private void printWeatherData() {
        if (!weatherDataByDate.isEmpty()) {
            ArrayList<Integer> valuesList = new ArrayList<>(outputFormatScheme.values());
            weatherDataByDate.forEach((row -> {
                int counter = 0;
                for (String element : row) {
                    System.out.printf("%-" + valuesList.get(counter) + "s", element);
                    counter++;
                }
                System.out.println();
            }));
        }
    }

    private String getDateFromString(String stringDate) throws NoSuchElementException {
        Matcher matcher = datePattern.matcher(stringDate);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new NoSuchElementException("Can't extract date from string");
    }

    private Document getPage() throws IOException {
        try {
            return Jsoup.parse(new URL(parseUrl), 3000);
        } catch (IOException e) {
            throw new IOException("df");
        }
    }
}
