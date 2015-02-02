import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class GraphBuilder {

    static Gson gson = new GsonBuilder().create();

    static HashMap<Organization, City> locatedCity = new HashMap<>();
    static HashMap<City, Region> locatedRegion = new HashMap<>();
    static HashMap<Region, Country> locatedCountry = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ArrayList<Organization> organizations = parseOrganizations();
        generateOrganizationCsv(organizations);
        generateRelationshipsCsv(organizations);
        generateCsvForEntities(organizations);
        generateAcquisitionEdges(organizations);
        generateInvestmentsEdges(organizations);
        generateGeoCsv();
    }

    public static ArrayList<Organization> parseOrganizations() throws Exception {
        ArrayList<Organization> result = new ArrayList<>();

        Files.walk(Paths.get("./data/organizations")).forEach(filePath -> {
            try {
                if (Files.isRegularFile(filePath)) {
                    String json = new String(Files.readAllBytes(filePath));
                    JsonElement elem = new JsonParser().parse(json);
                    Organization o = new Organization();
                    result.add(o);
                    JsonObject data = elem.getAsJsonObject().get("data").getAsJsonObject();
                    o.uuid = (data.has("uuid")) ? data.get("uuid").getAsString() : null;
                    o.type = (data.has("type")) ? data.get("type").getAsString() : null;
                    if (data.has("properties")) {
                        for (Map.Entry<String, JsonElement> e : data.get("properties").getAsJsonObject().entrySet()) {
                            if (e.getValue().isJsonPrimitive() && !e.getValue().isJsonArray()) {
                                JsonPrimitive p = e.getValue().getAsJsonPrimitive();
                                if (p.isBoolean()) {
                                    o.properties.put(e.getKey(), p.getAsBoolean());
                                } else if (p.isNumber()) {
                                    o.properties.put(e.getKey(), p.getAsNumber());
                                } else if (p.isString()) {
                                    o.properties.put(e.getKey(), p.getAsString());
                                }
                            }
                        }
                    }

                    if (data.has("relationships")) {
                        JsonObject rel = data.getAsJsonObject("relationships");

                        String type = "members";
                        if (rel.has(type)) {
                                JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                                for (JsonElement arrayElement : array) {
                                    o.relationships.add(gson.fromJson(arrayElement, Member.class));
                                }
                        }

                        type = "primary_image";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, PrimaryImage.class));
                            }
                        }

                        type = "current_team";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, TeamMember.class));
                            }
                        }

                        type = "founders";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, Founder.class));
                            }
                        }

                        type = "board_members_and_advisors";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, Advisor.class));
                            }
                        }

                        type = "offices";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                Office office = gson.fromJson(arrayElement, Office.class);
                                o.relationships.add(office);
                                saveLocationInfos(office, o);
                            }
                        }

                        type = "headquarters";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                HQ hq = gson.fromJson(arrayElement, HQ.class);
                                o.relationships.add(hq);
                                saveLocationInfos(hq, o);
                            }
                        }

                        type = "funding_rounds";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, FundingRound.class));
                            }
                        }

                        type = "categories";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, Category.class));
                            }
                        }

                        type = "websites";
                        if (rel.has(type)) {
                            JsonArray array = rel.getAsJsonObject(type).getAsJsonArray("items");
                            for (JsonElement arrayElement : array) {
                                o.relationships.add(gson.fromJson(arrayElement, Website.class));
                            }
                        }

                    }

                }
            } catch (Exception e) {
                System.err.println("Could not parse " + filePath);
            }
        });

        return result;
    }

    public static void generateOrganizationCsv(ArrayList<Organization> orgs) throws Exception {
        Set<String> properties = new HashSet<>();

        for (Organization o : orgs) {
            properties.addAll(o.properties.keySet().stream().collect(Collectors.toList()));
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("output/graph/organizations.csv"));
        writer.write("\"path\",\"uuid\",\"type\"");

        BufferedWriter jsonWriter = new BufferedWriter(new FileWriter("output/data.json"));

        for (String p : properties) {
            writer.write(",\"" + p + "\"");
        }

        boolean first = true;

        for (Organization o : orgs) {
            if (o.uuid == null ) {
                continue;
            }
            if (first) {
                first = false;
                jsonWriter.write("[");
            } else {
                jsonWriter.write(",\n");
            }
            jsonWriter.write("{\"name\":\"" + o.properties.get("permalink") + "\", \"code\":\"" + o.getPath() + "\"}");
            writer.write("\n\"" + o.getPath() + "\",\"" + o.uuid + "\",\"" + o.type + "\"");
            for (String p :  properties) {
                if (o.properties.containsKey(p)) {
                    if (o.properties.get(p) instanceof String) {
                        writer.write(",\"" + o.properties.get(p).toString().replace("\n", " ") + "\"");
                    } else {
                        writer.write("," + o.properties.get(p).toString());
                    }
                } else {
                    writer.write(",");
                }
            }
        }
        writer.close();
        jsonWriter.write("]");
        jsonWriter.close();
    }

    public static void generateRelationshipsCsv(ArrayList<Organization> orgs) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output/graph/relationships.csv"));

        writer.write("\"source\",\"target\",\"type\"");

        for (Organization o : orgs) {
            for (Relationship r : o.relationships) {
                if (r.getPath() == null || r.getPath().equals("null")) continue;
                writer.write("\n\"" + o.getPath() + "\",\"" + r.getPath() + "\",\"" + r.getRelType() + "\"");
            }
        }

        writer.close();

    }

    public static void generateCsvForEntities(ArrayList<Organization> orgs) throws Exception {
        Set<Relationship> set = new HashSet<>();

        for (Organization o : orgs) {
            set.addAll(o.relationships);
        }

        HashMap<String, BufferedWriter> writerHashMap = new HashMap<>();

        for (Relationship r : set) {
            Field[] fields = r.getClass().getFields();
            BufferedWriter writer;
            if (!writerHashMap.containsKey(r.getClass().getName())) {
                writer = new BufferedWriter(new FileWriter("output/graph/" + r.getClass().getSimpleName() +".csv"));
                writerHashMap.put(r.getClass().getName(), writer);

                for (int i=0; i < fields.length; i++) {
                    Field f = fields[i];
                    writer.write("\"" + f.getName() + "\"");
                    if (i != fields.length - 1) {
                        writer.write(",");
                    }
                }
            } else {
                writer = writerHashMap.get(r.getClass().getName());
            }
            writer.write("\n");
            for (int i=0; i < fields.length; i++) {
                Field f = fields[i];
                if (f.get(r) != null) {
                    writer.write("\"" + f.get(r) + "\"");
                } else {
                    if (f.getName().equals("path") && r instanceof Office) {
                        writer.write("\"" + r.getPath() + "\"");
                    }
                }
                if (i != fields.length - 1) {
                    writer.write(",");
                }
            }
        }

        for (BufferedWriter w : writerHashMap.values()) {
            w.close();
        }

    }

    public static void generateAcquisitionEdges(ArrayList<Organization> orgs) throws Exception {
        Set<String> orgsPath = new HashSet<>();
        for (Organization o : orgs) {
            orgsPath.add(o.getPath());
        }

        String json = new String(Files.readAllBytes(Paths.get("data/acquisitions.csv")));
        String[] lines = json.split("[\\r\\n]+");

        BufferedWriter writer = new BufferedWriter(new FileWriter("output/graph/acquisitions.csv"));
        writer.write("\"source\",\"target\",\"type\"");

        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
                continue;
            }
            if (line.equals(";;;;;;;;;;;;;;;;;;;;;")) continue;
            String[] parts = line.split(";");
            parts[0] = parts[0].substring(1,parts[0].length()); // remove first /
            parts[8] = parts[8].substring(1,parts[8].length()); // remove first /
            if (orgsPath.contains(parts[0]) || orgsPath.contains(parts[8])) {
                writer.write("\n\"" + parts[0] + "\",\"" + parts[8] + "\",\"acquiredBy\"");
            }
        }
        writer.close();
    }

    public static void generateInvestmentsEdges(ArrayList<Organization> orgs) throws Exception {
        Set<String> orgsPath = new HashSet<>();
        for (Organization o : orgs) {
            orgsPath.add(o.getPath());
        }

        String json = new String(Files.readAllBytes(Paths.get("data/investments.csv")));
        String[] lines = json.split("[\\r\\n]+");

        BufferedWriter writer = new BufferedWriter(new FileWriter("output/graph/investments.csv"));
        writer.write("\"source\",\"target\",\"type\"");

        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
                continue;
            }
            if (line.equals(";;;;;;;;;;;;;;;;;;;;;")) continue;
            String[] parts = line.split(";");
            if (parts[8].length() < 1 || parts[0].length() < 1) {
                continue;
            }
            parts[0] = parts[0].substring(1,parts[0].length()); // remove first /
            parts[8] = parts[8].substring(1,parts[8].length()); // remove first /
            if (orgsPath.contains(parts[0]) || orgsPath.contains(parts[8])) {
                writer.write("\n\"" + parts[8] + "\",\"" + parts[0] + "\",\"investedIn\"");
            }
        }
        writer.close();
    }

    public static void saveLocationInfos(Office office, Organization o) throws Exception {

        if (office.city_path != null) {
            City c = new City();
            c.path = office.city_path;
            c.name = office.city;
            locatedCity.put(o, c);
            if (office.region_path != null) {
                Region r = new Region();
                r.path = office.region_path;
                r.name = office.region;
                locatedRegion.put(c,r);
                if (office.country_path != null) {
                    Country co = new Country();
                    co.path = office.country_path;
                    co.name = office.country;
                    locatedCountry.put(r,co);
                }
            }
        }

    }

    public static void generateGeoCsv() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output/graph/GeoInformation.csv"));
        writer.write("\"src\",\"dst\",\"type\"");

        for (Map.Entry<Organization, City> e : locatedCity.entrySet()) {
            writer.write(",\n\"" + e.getKey().getPath() + "\",\"" + e.getValue().path + "\",\"locatedIn\"");
        }

        for (Map.Entry<City, Region> e : locatedRegion.entrySet()) {
            writer.write(",\n\"" + e.getKey().path + "\",\"" + e.getValue().path + "\",\"locatedIn\"");
        }

        for (Map.Entry<Region, Country> e : locatedCountry.entrySet()) {
            writer.write(",\n\"" + e.getKey().path + "\",\"" + e.getValue().path + "\",\"locatedIn\"");
        }

        writer.close();

        Set<Location> locations = new HashSet<>();
        locations.addAll(locatedRegion.keySet());
        locations.addAll(locatedCountry.keySet());
        locations.addAll(locatedCountry.values());

        HashMap<String, BufferedWriter> writerHashMap = new HashMap<>();

        for (Location l : locations) {
            Field[] fields = l.getClass().getFields();
            if (!writerHashMap.containsKey(l.getClass().getName())) {
                writer = new BufferedWriter(new FileWriter("output/graph/" + l.getClass().getSimpleName() +".csv"));
                writerHashMap.put(l.getClass().getName(), writer);

                for (int i=0; i < fields.length; i++) {
                    Field f = fields[i];
                    writer.write("\"" + f.getName() + "\"");
                    if (i != fields.length - 1) {
                        writer.write(",");
                    }
                }
            } else {
                writer = writerHashMap.get(l.getClass().getName());
            }
            writer.write("\n");
            for (int i=0; i < fields.length; i++) {
                Field f = fields[i];
                if (f.get(l) != null) {
                    writer.write("\"" + f.get(l) + "\"");
                }
                if (i != fields.length - 1) {
                    writer.write(",");
                }
            }
        }

        for (BufferedWriter w : writerHashMap.values()) {
            w.close();
        }

    }
}
