package com.theironyard.entities;

import javax.persistence.*;

/**
 * Created by lee on 11/3/16.
 */
@Entity
@Table(name = "loops")
public class Loop {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String voice;

    @Column(nullable = false)
    private Integer partid;

    public Loop() {
    }

    public Loop(String genre, String voice, Integer partid) {
        this.genre = genre;
        this.voice = voice;
        this.partid = partid;
    }

    public Integer getId() {
        return id;
    }

    public String getGenre() {
        return genre;
    }

    public String getVoice() {
        return voice;
    }

    public Integer getPartid() {
        return partid;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public void setPartid(Integer partid) {
        this.partid = partid;
    }

    @Override
    public String toString() {
        return genre + voice + partid;
    }
}
