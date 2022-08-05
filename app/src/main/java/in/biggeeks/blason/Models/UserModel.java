package in.biggeeks.blason.Models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String id;
    private String fcmToken;
    private String phone;
    private String name;
    private String age;
    private String gender;
    private String lang;
    private String photo;
    private String telegramID;
    private Boolean details1Uploaded;
    private Boolean details2Uploaded;
    private Double height = 0.0;
    private Double weight = 0.0;
    private String chronicDisease = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTelegramID() {
        return telegramID;
    }

    public void setTelegramID(String telegramID) {
        this.telegramID = telegramID;
    }

    public Boolean isDetails1Uploaded() {
        return details1Uploaded;
    }

    public void setDetails1Uploaded(Boolean details1Uploaded) {
        this.details1Uploaded = details1Uploaded;
    }

    public Boolean isDetails2Uploaded() {
        return details2Uploaded;
    }

    public void setDetails2Uploaded(Boolean details2Uploaded) {
        this.details2Uploaded = details2Uploaded;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getChronicDisease() {
        return chronicDisease;
    }

    public void setChronicDisease(String chronicDisease) {
        this.chronicDisease = chronicDisease;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserModel userModel = (UserModel) o;

        return id.equals(userModel.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
