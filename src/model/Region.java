package model;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author Tarek Auel
 * @since Februar 02, 2015.
 */
public class Region implements Location {

    public String path;
    public String name;

    @Override
    public boolean equals(Object o) {
        Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
            try {
                if (!Objects.equals(f.get(o), f.get(this))) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
    }

}
