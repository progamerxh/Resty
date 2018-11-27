package kuhiep.resty.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Restaurant reservation android application on 1/14/18.
 */

public class ReservationResponse {
    @SerializedName("time")
    private String time;
    @SerializedName("reservation")
    private ArrayList<Reservation> reservationList;

    public String getTime() {
        return time;
    }

    public ArrayList<Reservation> getReservationList() {
        return reservationList;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReservationList(ArrayList<Reservation> reservationList) {
        this.reservationList = reservationList;
    }
}
