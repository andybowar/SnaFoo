package snafoo.utilities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Snacks {

    /*
    `initialValue` is set to 999 because the Snack IDs in the API begin at 1000. This allows us
    to match snacks in the API with snacks in the repository so that every time the API is called,
    we can either verify that we already have the snack in the repository, or if we don't, it will
    be added.
     */
    @Id
    private Integer id;

    private String name;
    private boolean isOptional;
    private boolean isSuggested;
    private String purchaseLocations;
    private int numVotes;

    public Snacks() {

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Snacks))
            return false;
        if (obj == this)
            return true;
        return this.getId() == ((Snacks) obj).getId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurchaseLocations() {
        return purchaseLocations;
    }

    public void setPurchaseLocations(String purchaseLocations) {
        this.purchaseLocations = purchaseLocations;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(int numVotes) {
        this.numVotes = numVotes;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    public boolean isSuggested() {
        return isSuggested;
    }

    public void setSuggested(boolean suggested) {
        isSuggested = suggested;
    }

    public Snacks(String name, String purchaseLocations, int numVotes, boolean isOptional) {
        this.name = name;
        this.purchaseLocations = purchaseLocations;
        this.numVotes = numVotes;
        this.isOptional = isOptional;
    }
}
