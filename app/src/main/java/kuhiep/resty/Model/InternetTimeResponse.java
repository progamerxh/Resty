package kuhiep.resty.Model;

import com.google.gson.annotations.SerializedName;

/**
 * Restaurant reservation android application on 1/15/18.
 */

public class InternetTimeResponse {
    @SerializedName("time")
    private String time;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}

