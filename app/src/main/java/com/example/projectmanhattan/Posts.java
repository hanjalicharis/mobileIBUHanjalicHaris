package com.example.projectmanhattan;

public class Posts
{

    private String UserID;
    private String Time;
    private String ProfileImgURL;
    private String ImgURL;
    private String FullName;
    private String Description;
    private String Date;

    public Posts()
    {
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getProfileImgURL() {
        return ProfileImgURL;
    }

    public void setProfileImgURL(String profileImgURL) {
        ProfileImgURL = profileImgURL;
    }

    public String getImgURL() {
        return ImgURL;
    }

    public void setImgURL(String imgURL) {
        ImgURL = imgURL;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public Posts(String userID, String time, String profileImgURL, String imgURL, String fullName, String description, String date) {
        UserID = userID;
        Time = time;
        ProfileImgURL = profileImgURL;
        ImgURL = imgURL;
        FullName = fullName;
        Description = description;
        Date = date;
    }
}
