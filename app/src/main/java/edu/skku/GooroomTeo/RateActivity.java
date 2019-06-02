package edu.skku.GooroomTeo;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RateActivity extends AppCompatActivity {

    Button alertwrongbtn, ratesendbtn;
    ListView listView;

    ArrayList<String> data;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        alertwrongbtn = findViewById(R.id.alertwrongbtn);
        ratesendbtn = findViewById(R.id.ratesendbtn);
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

        ratesendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialog_view = inflater.inflate(R.layout.dialog_rate, null);
                alert_body.setView(dialog_view);


                final Button sendbtn = view.findViewById(R.id.sendbtn);
                final Button cancelbtn = view.findViewById(R.id.cancelbtn);

                final EditText rateet = dialog_view .findViewById(R.id.rateet);
                final EditText commentet = dialog_view .findViewById(R.id.commentet);

                final AlertDialog alert = alert_body.create();

                alert_body.setPositiveButton("보내기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Send message to server
                        String comment = commentet.getText().toString();
                        if(comment.length() <= 0){
                            Toast.makeText(RateActivity.this, "코멘트를 입력하십시오", Toast.LENGTH_LONG).show();
                        }
                        else{ // Send info to Server

                        }


                        alert.dismiss();
                    }
                });
                alert_body.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel
                        Toast.makeText(RateActivity.this, "평가 취소하셨습니다.", Toast.LENGTH_LONG).show();
                        alert.dismiss();

                    }
                });

                alert.setTitle("이 장소 평가");
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
