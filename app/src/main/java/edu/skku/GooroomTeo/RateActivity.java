package edu.skku.GooroomTeo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RateActivity extends AppCompatActivity {

    private DatabaseReference DBReference;
    private StorageReference STReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText commentet;
    private Spinner spinner;
    private TextView locnametv;
    private TextView avgratetv;
    private ImageView locimg;
    private Button alertwrongbtn;
    private Button ratesendbtn;
    private ListView listView;
    private Intent intent;

    private ArrayList<Integer> RateInt;
    private ArrayList<String> RateandCommentList;
    private ArrayAdapter<String> adapter;

    private AlertDialog alert;
    private Bitmap bm;
    private Thread mthread;
    private URL url;

    private Long time;
    private int rate;
    private String comment;
    private String BuildingName;
    private String refer_path;
    private boolean isAlertOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        locnametv = findViewById(R.id.locnametv);
        avgratetv = findViewById(R.id.avgratetv);
        locimg = findViewById(R.id.locimg);
        alertwrongbtn = findViewById(R.id.alertwrongbtn);
        ratesendbtn = findViewById(R.id.ratesendbtn);
        listView = findViewById(R.id.listview);

        intent = getIntent();
        BuildingName = intent.getStringExtra("information");
        refer_path = "locinfo/" + BuildingName + "/userrate";
        locnametv.setText(BuildingName);

        DBReference = FirebaseDatabase.getInstance().getReference();
        //STReference = FirebaseStorage.getInstance().getReference().child("images/"+BuildingName+".jpg");
        STReference = FirebaseStorage.getInstance().getReference().child("images/test.jpg");
        STReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri _uri) {
                try{
                    //Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    //locimg.setImageBitmap(bm);
                    url = new URL(_uri.toString());
                    mthread.start();
                    try{
                        mthread.join();
                        locimg.setImageBitmap(bm);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        mthread = new Thread(){
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    bm = BitmapFactory.decodeStream(is);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        RateandCommentList = new ArrayList<>();
        RateInt = new ArrayList<>();
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
                // Calculate average rate and set
                String key = dataSnapshot.getKey();

                UserRateInfo get = dataSnapshot.getValue(UserRateInfo.class);
                comment = get.comment;
                rate = get.rate;
                String rate_str = Integer.toString(get.rate);

                RateInt.add(rate);
                RateandCommentList.add("평점: " + rate + "\n코멘트: " + comment);
                adapter.clear();
                adapter.addAll(RateandCommentList);
                int total = RateInt.size();
                double average_rate = 0;
                for (int i = 0; i < total; i++) {
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

        if (savedInstanceState != null) {
            isAlertOn = savedInstanceState.getBoolean("is_alert_on");
            if(isAlertOn){
                RateSendDialog();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_alert_on", isAlertOn);
    }

    private void WrongInfoSendDialog() {
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
        spinner = dialog_view.findViewById(R.id.ratespinner);
        commentet = dialog_view.findViewById(R.id.commentet);

        alert = alert_body.create();

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
                } else {
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

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isAlertOn = false;
            }
        });

        alert.setTitle("이 장소 평가");
        alert.show();
        isAlertOn = true;
    }

    private void postFirebaseDatabase() {
        /**
         *  Post data to firebase
         */
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        time = System.currentTimeMillis();
        UserRateInfo post = new UserRateInfo(rate, comment);
        postValues = post.toMap();

        childUpdates.put(refer_path + "/" + time.toString(), postValues);
        DBReference.updateChildren(childUpdates);
    }

}