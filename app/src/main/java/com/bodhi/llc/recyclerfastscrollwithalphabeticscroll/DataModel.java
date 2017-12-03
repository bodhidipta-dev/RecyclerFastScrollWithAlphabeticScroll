package com.bodhi.llc.recyclerfastscrollwithalphabeticscroll;

/**
 * Created by Bhaiya on 12/2/2017.
 */

public class DataModel {
    String fastName;
    String lastname;

    public DataModel(String fastName, String lastname) {
        this.fastName = fastName;
        this.lastname = lastname;
    }

    public String getFastName() {
        return fastName;
    }

    public void setFastName(String fastName) {
        this.fastName = fastName;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
