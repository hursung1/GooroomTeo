package edu.skku.GooroomTeo;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RateActivity extends AppCompatActivity {

    Button alertwrongbtn;
    ListView listView;

    ArrayList<String> data;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        alertwrongbtn = findViewById(R.id.alertwrongbtn);
        listView = findViewById(R.id.listview);


        data = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        data.add("111");
        arrayAdapter.addAll(data);

        listView.setAdapter(arrayAdapter);

        alertwrongbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show alert
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
        });
    }

    private void getFirebaseDatabase(){
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

}
