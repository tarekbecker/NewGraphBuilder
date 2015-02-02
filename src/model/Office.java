package model;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class Office extends Relationship {

    static MessageDigest md;
    static { try { md = MessageDigest.getInstance("SHA-256"); } catch (Exception e) {}}

    public String path;
    public String type;
    public String name;
    public String street1;
    public String street2;
    public String postal_code;
    public String city;
    public String city_path;
    public String region;
    public String region_path;
    public String country;
    public String country_path;
    public double latitude;
    public double longitude;

    @Override
    public String getPath() {

        try {
            return Base64.getEncoder().encodeToString((type + name + street1 + street2 + postal_code + city).getBytes("UTF-8"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getRelType() {
        return "hasOffice";
    }
}
