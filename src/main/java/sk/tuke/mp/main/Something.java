package sk.tuke.mp.main;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Something {


    @Id
    public int Id;

    public String str;

}
