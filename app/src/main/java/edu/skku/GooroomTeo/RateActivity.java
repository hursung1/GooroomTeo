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

    private AlertDialog wronginfo_alert;
    private Bitmap bm = null;
    private Thread mthread;
    private URL url;

    private Long time;
    private int rate;
    private String comment;
    private String BuildingName;
    private String refer_path_userrate;
    private String refer_path_wronginfocmts;
    private boolean wronginfoAlert = false;
    private boolean whichAlert = false;
    private boolean commentAlert = false;


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
        refer_path_userrate = "locinfo/" + BuildingName + "/userrate";
        refer_path_wronginfocmts = "locinfo/" + BuildingName + "/wronginfocmt";
        locnametv.setText(BuildingName);

        DBReference = FirebaseDatabase.getInstance().getReference();
        STReference = FirebaseStorage.getInstance().getReference().child("images/" + BuildingName);
        //STReference = FirebaseStorage.getInstance().getReference().child("images/test");
        STReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri _uri) {
                if (bm == null) {
                    try {
                        url = new URL(_uri.toString());
                        mthread.start();
                        try {
                            mthread.join();
                            locimg.setImageBitmap(bm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    locimg.setImageBitmap(bm);
                }
            }
        });

        mthread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    bm = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
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
                DialogCall(true);
            }
        });

        //Get User comment from firebase database
        DBReference.child(refer_path_userrate).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Calculate average rate and set

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
            wronginfoAlert = savedInstanceState.getBoolean("wronginfo_alert");
            commentAlert = savedInstanceState.getBoolean("comment_alert");
            whichAlert = savedInstanceState.getBoolean("which_alert");
            bm = savedInstanceState.getParcelable("image");
            if (wronginfoAlert) {
                WrongInfoSendDialog();
            }
            if (commentAlert) {
                DialogCall(whichAlert);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("wronginfo_alert", wronginfoAlert);
        outState.putBoolean("comment_alert", commentAlert);
        outState.putBoolean("which_alert", whichAlert);
        outState.putParcelable("image", bm);
    }

    private void WrongInfoSendDialog() {
        /**
         *  Send 'Wrong info' message to server
         */
        final AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
        alert_body.setTitle("잘못된 정보 신고");
        alert_body.setMessage("잘못된 정보를 신고할까요?");
        alert_body.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                wronginfo_alert.dismiss();
                DialogCall(false);
                //Toast.makeText(RateActivity.this, "신고", Toast.LENGTH_LONG).show();
                // Send message to server
            }
        });
        alert_body.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancel
                wronginfo_alert.dismiss();
                Toast.makeText(RateActivity.this, "신고 취소", Toast.LENGTH_LONG).show();
            }
        });

        alert_body.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                wronginfoAlert = false;
            }
        });
        wronginfo_alert = alert_body.create();
        wronginfo_alert.show();
        wronginfoAlert = true;
    }

    private void DialogCall(final boolean isRate) { // if isRate is true --> rating, else --> send wrong info
        /**
         *  Function that gets rate & comment from user.
         *  Then, call postFirebaseDatabase()
         *
         */

        whichAlert = isRate;
        String alert_txtfield = "어떤 정보가 잘못되었나요?";

        final AlertDialog comment_alert;
        AlertDialog.Builder alert_body = new AlertDialog.Builder(RateActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_rate, null);
        alert_body.setView(dialog_view);

        Button sendbtn = dialog_view.findViewById(R.id.sendbtn);
        Button cancelbtn = dialog_view.findViewById(R.id.cancelbtn);
        TextView ratetv = dialog_view.findViewById(R.id.ratetv);
        TextView alert_commenttv = dialog_view.findViewById(R.id.alert_commenttv);
        spinner = dialog_view.findViewById(R.id.ratespinner);
        commentet = dialog_view.findViewById(R.id.commentet);

        comment_alert = alert_body.create();

        if (isRate) {
            alert_txtfield = "이 장소는 어떤가요?";

            ratetv.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // Spinner Selected Listener
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    rate = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        alert_commenttv.setText(alert_txtfield);

        sendbtn.setOnClickListener(new View.OnClickListener() { // Send button OnClickListener
            @Override
            public void onClick(View view) {
                // Send message to server
                comment = commentet.getText().toString();
                if (comment.length() <= 0) {
                    Toast.makeText(RateActivity.this, "내용을 적어주세요.", Toast.LENGTH_LONG).show();
                } else {
                    // Send info to Server
                    postFirebaseDatabase(isRate);
                    comment_alert.dismiss();
                }

            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() { // Cancel button OnClickListener
            @Override
            public void onClick(View view) {
                // Cancel
                Toast.makeText(RateActivity.this, "평가를 취소했어요.", Toast.LENGTH_LONG).show();
                comment_alert.dismiss();
            }
        });

        comment_alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                commentAlert = false;
            }
        });

        comment_alert.setTitle("이 장소 평가");
        comment_alert.show();
        commentAlert = true;
    }

    private void postFirebaseDatabase(boolean isRate) {
        /**
         *  Post data to firebase
         */
        String refer_path;
        UserRateInfo post;
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        time = System.currentTimeMillis();
        if (isRate) {
            post = new UserRateInfo(rate, comment);
            refer_path = refer_path_userrate;
        } else {
            post = new UserRateInfo(comment);
            refer_path = refer_path_wronginfocmts;
        }
        postValues = post.toMap();
        childUpdates.put(refer_path + "/" + time.toString(), postValues);
        DBReference.updateChildren(childUpdates);
    }

}