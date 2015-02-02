package model;

/**
 * @author Tarek Auel
 * @since Februar 01, 2015.
 */
public class HQ extends Office {

    @Override
    public String getRelType() {
        return "hasHeadquarter";
    }
}
