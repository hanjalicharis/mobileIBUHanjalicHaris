package com.example.projectmanhattan;

public class FindFriends
{
    private String ProfileImage;
    private String fullname;
    private String status;

    public FindFriends()
    {
    }

    public FindFriends(String profileImage, String fullname, String status) {
        ProfileImage = profileImage;
        this.fullname = fullname;
        this.status = status;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
