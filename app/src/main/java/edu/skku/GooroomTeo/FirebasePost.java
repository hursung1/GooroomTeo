package edu.skku.GooroomTeo;
import java.util.HashMap;
import java.util.Map;
public class FirebasePost {
    public double latitude;
    public double longitude;

    public FirebasePost() {
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return result;
    }
}