package sk.tuke.mp.core.sql.decorators;

import sk.tuke.mp.core.sql.Constants;
import sk.tuke.mp.core.sql.Field;

public class PrimaryKeyField extends Field implements AdditionalDecorator {


    public PrimaryKeyField(Field f) {
        super(f);
        // addDecorator(new NotNullField(f));
        // addDecorator(new AutoIncrementField(f));
    }


    @Override
    public String toStringDecoration() {
        return String.format("PRIMARY KEY (%s)", getName());
    }
}
