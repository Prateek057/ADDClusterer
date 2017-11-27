package model;

import org.mongodb.morphia.annotations.Entity;

import java.util.List;

@Entity
public class Algorithm {


    private String id;
    private String name;
    private List<Option> options;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }



}
