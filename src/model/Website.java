package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class Website extends Relationship {

    public String url;
    public String type;
    public String title;

    @Override
    public String getPath() {
        return url;
    }

    @Override
    public String getRelType() {
        return "hasWebsite";
    }
}
