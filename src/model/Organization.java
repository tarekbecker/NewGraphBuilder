package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class Organization {

    public String uuid;
    public String type;
    public HashMap<String, Object> properties = new HashMap<>();
    public Set<Relationship> relationships = new HashSet<>();

    public String getPath() {
        if (properties.containsKey("permalink")) {
            return type.toLowerCase() + "/" + properties.get("permalink");
        } else {
            if (type == null) {
                return null;
            } else {
                return type.toLowerCase() + "/" + uuid;
            }
        }
    }
}
