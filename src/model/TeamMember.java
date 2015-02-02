package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class TeamMember extends Relationship {

    public String path;
    public String first_name;
    public String last_name;
    public String title;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getRelType() {
        return "hasTeamMember";
    }
}
