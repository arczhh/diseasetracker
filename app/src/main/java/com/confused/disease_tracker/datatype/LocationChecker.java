package com.confused.disease_tracker.datatype;

public class LocationChecker {
    private String patientName, patientDisease, patientStatus;
    private MyLocation usrLocation, patLocation;
    private double distance;

    public LocationChecker(Patient pat, MyLocation usrLocation, MyLocation patLocation, double distance){
        this.patientName = pat.getPatientName();
        this.patientDisease = pat.getPatientDisease();
        this.patientStatus = pat.getPatientStatus();
        this.usrLocation = usrLocation;
        this.patLocation = patLocation;
        this.distance = distance;
    }

    public String getMsg() {
        return "ตรวจพบตำแหน่งตรงกันกับคุณ"+this.patientName+", ผู้ป่วยโรค "+this.patientDisease+" ซึ่งอยู่ห่างกันเป็นระยะ: "+this.distance+" กม. เมื่อเวลาช่วง "+this.usrLocation.getTimestamp();
    }
}
