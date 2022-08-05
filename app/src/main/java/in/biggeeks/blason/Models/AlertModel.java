package in.biggeeks.blason.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class AlertModel implements Serializable {

    public enum AlertStatus {
        ACTIVE,
        RESPONDED,
        CANCELLED,
        FINISHED
    }

    public enum AlertLevel {
        EXTREME,
        MODERATE,
        MILD
    }

    private String id;
    private AlertStatus alertStatus;

    private AlertLevel alertLevel;
    private String triggerText;
    private Timestamp triggerTime;
    private GeoPoint victimLoc = new GeoPoint(0.0, 0.0);
    private String victimID;
    private ArrayList<String> receiverIDs = new ArrayList<>();
    private ArrayList<String> seenByIDs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }

    public Timestamp getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Timestamp triggerTime) {
        this.triggerTime = triggerTime;
    }

    public GeoPoint getVictimLoc() {
        return victimLoc;
    }

    public void setVictimLoc(GeoPoint victimLoc) {
        this.victimLoc = victimLoc;
    }

    public String getVictimID() {
        return victimID;
    }

    public void setVictimID(String victimID) {
        this.victimID = victimID;
    }

    public ArrayList<String> getReceiverIDs() {
        return receiverIDs;
    }

    public void setReceiverIDs(ArrayList<String> receiverIDs) {
        this.receiverIDs = receiverIDs;
    }

    public ArrayList<String> getSeenByIDs() {
        return seenByIDs;
    }

    public void setSeenByIDs(ArrayList<String> seenByIDs) {
        this.seenByIDs = seenByIDs;
    }
}
