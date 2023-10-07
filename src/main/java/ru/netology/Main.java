package ru.netology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String csvToJson = "data.json";
        String xmlToJson = "data2.json";
        String xmlFileName = "data.xml";

        //----------------CSV to JSON----------------//
        List<Employee> listCSV = parseCSV(columnMapping, fileName);
        String csvJson = listToJson(listCSV);
        writeString(csvJson, csvToJson);

        //----------------XML to JSON----------------//
        List<Employee> listXML = parseXML(xmlFileName);
        String xmlJson = listToJson(listXML);
        writeString(xmlJson, xmlToJson);

        //----------------Reader JSON----------------//
        String json = readString(csvToJson);
        List<Employee> list = jsonToList(json);

        for (Employee line: list) {
            System.out.println(line);
        }

    }

    private static String readString(String csvToJson) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvToJson))) {
            String s;
            String json = "";
            while ((s = bufferedReader.readLine()) != null) {
                json += s;
            }

            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> employeeList = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(json);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                Employee employee = gson.fromJson(jsonObject.toString(), Employee.class);
                employeeList.add(employee);
            }

            return employeeList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Employee> parseXML(String xmlFileName) {
        List<Employee> employees = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(xmlFileName));

            Node root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i) instanceof Element) {
                    Element employeeElement = (Element) nodeList.item(i);
                    long id = Integer.parseInt(employeeElement.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = employeeElement.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = employeeElement.getElementsByTagName("lastName").item(0).getTextContent();
                    int age = Integer.parseInt(employeeElement.getElementsByTagName("age").item(0).getTextContent());
                    String country = employeeElement.getElementsByTagName("country").item(0).getTextContent();

                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    employees.add(employee);
                }
            }

            return employees;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeString(String json, String outFileName) {
        try (FileWriter fileWriter = new FileWriter(outFileName, true)) {
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        return gson.toJson(list, listType);
    }

    private static List parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csv = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csvtb = new CsvToBeanBuilder<Employee>(csv)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> list = csvtb.parse();

            return list;
        } catch (IOException e) {
            return null;
        }
    }
}