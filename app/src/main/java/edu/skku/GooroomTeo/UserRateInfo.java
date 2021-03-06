package edu.skku.GooroomTeo;

import java.util.HashMap;
import java.util.Map;

public class UserRateInfo {
    public Integer rate;
    public String comment;


    public UserRateInfo(){}
    public UserRateInfo(String comment){
        this.comment = comment;
    }
    public UserRateInfo(int rate, String comment) {
        this.rate = rate;
        this.comment = comment;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("rate", rate);
        result.put("comment", comment);
        return result;
    }
}
