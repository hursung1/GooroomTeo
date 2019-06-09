package edu.skku.GooroomTeo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RateActivity extends AppCompatActivity {

    private DatabaseReference DBReference;
    private TextView locnametv;
    private TextView avgratetv;
    private Button alertwrongbtn;
    private Button ratesendbtn;
    private ListView listView;
    private Intent intent;

    private ArrayList<Integer> RateInt;
    private ArrayList<String> RateandCommentList;
    private ArrayAdapter<String> adapter;

    private long time;
    private int rate;
    private String comment;
    private String BuildingName;
    private String refer_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        locnametv = findViewById(R.id.locnametv);
        avgratetv = findViewById(R.id.avgratetv);
        alertwrongbtn = findViewById(R.id.alertwrongbtn);
        ratesendbtn = findViewById(R.id.ratesendbtn);
        listView = findViewById(R.id.listview);

        intent = getIntent();
        BuildingName = intent.getStringExtra("information");
        refer_path = "locinfo/" + BuildingName;
        locnametv.setText(BuildingName);

        DBReference = FirebaseDatabase.getInstance().getReference(refer_path);

        RateandCommentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        //Wrong info
        alertwrongbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WrongInfoSendDialog();
            }
        });

        //Rating
        ratesendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RateSendDialog();
            }
        });

        //Get User comment from firebase database
        DBReference.child(refer_path).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                //System.out.println(key);
                UserRateInfo get = dataSnapshot.getValue(UserRateInfo.class);

                comment = get.comment;
                rate = get.rate;
                String rate_str = Integer.toString(get.rate);

                RateInt.add(rate);
                RateandCommentList.add("평점: " + rate + "\n코멘트: " + comment);
                adapter.clear();
                adapter.addAll(RateandCommentList);

                // Calculate average rate and set
                double average_rate = 0;
                int total = RateInt.size();
                for(int i = 0; i < total; i ++){
                    average_rate += RateInt.get(i);
                }
                average_rate /= total;
                avgratetv.setText("평점(평균): " + String.format("%.2f", average_rate));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void WrongInfoSendDialog(){
        /**
         *  Send 'Wrong info' message to server
         */
        AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
        alert_body.setMessage("잘못된 정보를 신고하시겠습니까?");
        alert_body.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(RateActivity.this, "신고", Toast.LENGTH_LONG).show();
                // Send message to server
            }
        });
        alert_body.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancel
                Toast.makeText(RateActivity.this, "신고 취소", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alert = alert_body.create();
        alert.setTitle("잘못된 정보 신고");
        alert.show();
    }

    private void RateSendDialog() {
        /**
         *  Function that gets rate & comment from user.
         *  Then, call postFirebaseDatabase()
         *
         */
        AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_rate, null);
        alert_body.setView(dialog_view);

        final Button sendbtn = dialog_view.findViewById(R.id.sendbtn);
        final Button cancelbtn = dialog_view.findViewById(R.id.cancelbtn);
        final Spinner spinner = dialog_view.findViewById(R.id.ratespinner);
        final EditText commentet = dialog_view.findViewById(R.id.commentet);

        final AlertDialog alert = alert_body.create();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // Spinner Selected Listener
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rate = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() { // Send button OnClickListener
            @Override
            public void onClick(View view) {
                // Send message to server
                comment = commentet.getText().toString();
                if (comment.length() <= 0) {
                    Toast.makeText(RateActivity.this, "코멘트를 입력하십시오", Toast.LENGTH_LONG).show();
                }
                else {
                    // Send info to Server
                    postFirebaseDatabase();
                    alert.dismiss();
                }

            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() { // Cancel button OnClickListener
            @Override
            public void onClick(View view) {
                // Cancel
                Toast.makeText(RateActivity.this, "평가 취소하셨습니다.", Toast.LENGTH_LONG).show();
                alert.dismiss();
            }
        });

        alert.setTitle("이 장소 평가");
        alert.show();
    }


    private void postFirebaseDatabase(){
        /**
         *  Post data to firebase
         *  !!!!MODIFY REQUIRED!!!
         */
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        time = System.currentTimeMillis();
        UserRateInfo post = new UserRateInfo();
        postValues = post.toMap();

        childUpdates.put(refer_path+"/userrate/"+time, postValues);
        DBReference.updateChildren(childUpdates);
    }
    /*void clearET() {
        edittext_msg.setText("");
    }*/

}