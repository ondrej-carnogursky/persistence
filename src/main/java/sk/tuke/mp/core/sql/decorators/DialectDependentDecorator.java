package sk.tuke.mp.core.sql.decorators;

import sk.tuke.mp.core.sql.commands.SQLDialects;

public interface DialectDependentDecorator {

    public String toStringDecoration(SQLDialects dialect);

}
