package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class Member extends Relationship{

    public String type;
    public String name;
    public String path;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getRelType() {
        return "isMemberOf";
    }
}
