package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class FundingRound extends Relationship {

    public String path;
    public String name;
    public String type;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getRelType() {
        return "hasFundingRound";
    }
}
