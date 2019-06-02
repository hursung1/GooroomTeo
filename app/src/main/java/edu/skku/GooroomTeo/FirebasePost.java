package edu.skku.GooroomTeo;
import java.util.HashMap;
import java.util.Map;
public class FirebasePost {
    public double latitude;
    public double longitude;
    public String name;

    public FirebasePost() {
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("name", name);
        return result;
    }
}