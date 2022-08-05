package in.biggeeks.blason.Models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class NearOnesModel implements Serializable {
    String id;
    String relation;
    Timestamp addedOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Timestamp getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Timestamp addedOn) {
        this.addedOn = addedOn;
    }
}
