package sk.tuke.mp.core.sql.decorators;

import sk.tuke.mp.core.sql.Constants;
import sk.tuke.mp.core.sql.Field;

public class AutoIncrementField extends Field {


    public AutoIncrementField(Field f) {
        super(f);
    }


    public String toStringDecoration() {
        return "AUTO_INCREMENT";
    }

}
