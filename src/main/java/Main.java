import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {

        String fileCsvName = "data.csv";
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> listByCsv = parseCSV(columnMapping, fileCsvName);
        String jsonCSV = listToJson(listByCsv);
        writeString(jsonCSV, "data.json");

        String fileXmlName = "data.xml";
        List<Employee> listByXml = parseXML("data.xml");
        String jsonXml = listToJson(listByXml);
        writeString(jsonXml, "data2.json");

        String json = readString("data.json");
        List<Employee> list = jsonToList(json);
        list.forEach(System.out::println);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileCsvName) {
        List<Employee> employeeListCsv = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileCsvName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            employeeListCsv = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employeeListCsv;
    }

    private static List<Employee> parseXML(String fileXmlName) {
        Document doc = null;
        List<Employee> employeeListXml = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(fileXmlName));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        Node root = doc.getDocumentElement();
        NodeList nList = root.getChildNodes();

        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element element = (Element) node;

                employeeListXml.add(new Employee(
                        Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent()),
                        element.getElementsByTagName("firstName").item(0).getTextContent(),
                        element.getElementsByTagName("lastName").item(0).getTextContent(),
                        element.getElementsByTagName("country").item(0).getTextContent(),
                        Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent())
                ));
            }
        }
        return employeeListXml;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .setPrettyPrinting()
                .create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();

        return gson.toJson(list, listType);
    }

    private static void writeString(String json, String fileExitName) {
        try (FileWriter file = new
                FileWriter(fileExitName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readString(String json) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(json))) {
            String s;
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        Gson gson = new GsonBuilder()
                .create();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json);
            for (Object obj : array) {
                list.add(gson.fromJson(String.valueOf(obj), Employee.class));
            }
        } catch (JsonSyntaxException | ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}