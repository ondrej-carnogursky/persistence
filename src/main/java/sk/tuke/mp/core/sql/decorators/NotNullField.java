package sk.tuke.mp.core.sql.decorators;

import sk.tuke.mp.core.sql.Constants;
import sk.tuke.mp.core.sql.Field;

public class NotNullField extends Field {


    public NotNullField(Field f) {
        super(f);
    }


    public String toStringDecoration() {
        return "NOT NULL";
    }

}
