package com.example.ucityventure;

public class ScanConfirmation {

    String PIN, ProviderID, UserID, Status;

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getProviderID() {
        return ProviderID;
    }

    public void setProviderID(String providerID) {
        ProviderID = providerID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public String toString() {
        return "ScanConfirmation{" +
                "PIN='" + PIN + '\'' +
                ", ProviderID='" + ProviderID + '\'' +
                ", UserID='" + UserID + '\'' +
                ", Status='" + Status + '\'' +
                '}';
    }
}
