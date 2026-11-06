package com.tick_tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity //this tells JPA to make database table for this class
public class Sighting {

    @Id  //Primary Key
    private String id;

    @Column(nullable = false) //Column here makes the database reject row where value is missing(same with the others fields)
    private String location;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String species;

    @Column(nullable = false)
    private String latinName;

    protected Sighting() {
        // JPA needs this empty constructor to create the object when loading a row
    }

    public Sighting(String id, String location, LocalDateTime dateTime, String species, String latinName) {
        this.id = id;
        this.location = location;
        this.dateTime = dateTime;
        this.species = species;
        this.latinName = latinName;


    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getSpecies() {
        return species;
    }

    public String getLatinName() {
        return latinName;
    }

}
