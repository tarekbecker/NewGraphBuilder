package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class PrimaryImage extends Relationship {

    public String type;
    public String title;
    public String path;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getRelType() {
        return "hasPrimaryImage";
    }
}
