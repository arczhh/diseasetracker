package com.confused.disease_tracker.datatype;

public class LocationChecker {
    private String patientName, patientDisease, patientStatus;
    private MyLocation usrLocation, patLocation;
    private double distance;
    private int risk;

    public LocationChecker(Patient pat, MyLocation usrLocation, MyLocation patLocation, double distance, int risk){
        this.patientName = pat.getPatientName();
        this.patientDisease = pat.getPatientDisease();
        this.patientStatus = pat.getPatientStatus();
        this.usrLocation = usrLocation;
        this.patLocation = patLocation;
        this.distance = distance;
        this.risk = risk;
    }

    public int getRisk(){
        return this.risk;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientDisease() {
        return patientDisease;
    }

    public void setPatientDisease(String patientDisease) {
        this.patientDisease = patientDisease;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public MyLocation getUsrLocation() {
        return usrLocation;
    }

    public void setUsrLocation(MyLocation usrLocation) {
        this.usrLocation = usrLocation;
    }

    public MyLocation getPatLocation() {
        return patLocation;
    }

    public void setPatLocation(MyLocation patLocation) {
        this.patLocation = patLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public String getMsg() {
        return "ตรวจพบตำแหน่งตรงกันกับคุณ"+this.patientName+", ผู้ป่วยโรค "+this.patientDisease+" ซึ่งอยู่ห่างกันเป็นระยะ: "+this.distance+" กม. เมื่อเวลาช่วง "+this.usrLocation.getTimestamp();
    }
}
