package edu.skku.GooroomTeo;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RateActivity extends AppCompatActivity {

    Button alertwrongbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        alertwrongbtn = findViewById(R.id.alertwrongbtn);
        alertwrongbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
                alert_body.setMessage("잘못된 정보를 신고하시겠습니까?");
                alert_body.setPositiveButton("네", null);
                alert_body.setNegativeButton("아니요", null);

                AlertDialog alert = alert_body.create();
                alert.setTitle("잘못된 정보 신고");
            }
        });
    }
}
