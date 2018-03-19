package sk.tuke.mp.main;

import sk.tuke.mp.persistence.PersistenceManager;
import sk.tuke.mp.persistence.ReflectivePersistenceManager;

import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) throws Exception {
        PersistenceManager mngr = new ReflectivePersistenceManager(DriverManager.getConnection("jdbc:sqlite::memory:"));

        mngr.createTables(Something.class);


        Something sth = new Something();

        sth.Id = 1;
        sth.str = "hola";

        mngr.save(sth);
    }
}
