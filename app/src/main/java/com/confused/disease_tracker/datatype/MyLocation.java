package com.confused.disease_tracker.datatype;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDateTime;

public class MyLocation {
    private LatLng latLng;
    private LocalDateTime timestamp;

    public MyLocation(LatLng lat, LocalDateTime timestamp) {
        this.latLng = lat;
        this.timestamp = timestamp;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng lat) {
        this.latLng = lat;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
