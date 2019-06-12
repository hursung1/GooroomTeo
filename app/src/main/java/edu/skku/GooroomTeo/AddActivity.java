package edu.skku.GooroomTeo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddActivity extends AppCompatActivity {
    final private static int REQUEST_IMAGE_CAPTURE = 1;
    final private static int RESULT_LOAD_IMAGE = 2;
    final private static int REQUEST_CAMERA_PERMISSION_GRANTED = 3;
    final private static int REQUEST_STORAGE_PERMISSION_GRANTED = 4;

    int accepted = 0;
    private ImageView imagePreview;
    private TextView uploadTextView, ocrResultText, noResultText;
    private EditText nameTextView;
    private Button regButton, scanImage, resultPage;
    private Bitmap image;
    private static String imageFilePath;
    private DatabaseReference DBReference = FirebaseDatabase.getInstance().getReference();
    String ocrResult;
    private Button cameraButton, galleryImageButton;
    private StorageReference mStorageRef;
    private String imageFilePath2;

    private Uri pu;

    double lat, lon;
    String locname;


    // 위도, 경도 구하기(GPS 사용)
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            lon = location.getLongitude();
            lat = location.getLatitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    // 위도, 경도 구하기(network 사용)
    LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            lon = location.getLongitude();
            lat = location.getLatitude();
            makeToastText(provider + " " + lat + " " + lon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    //Path 구하기
    static String getRealPathFromURI(Context context, Uri uri2) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri2, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    //이미지 파일 방향 세팅
    static Bitmap getCorrectOrientedImage(String imageFilePath) {
        ExifInterface exifInterface = null;

        Matrix matrix = new Matrix();
        try {
            exifInterface = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270);
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //OCR(글자 인식)
    static String textRecognition(Bitmap image, Context context) {
        String ocrResult = "";
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(image).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append(" ");
            }
            ocrResult = String.valueOf(stringBuilder);
        }
        return ocrResult;
    }

    // 이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath2 = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        cameraButton = (Button) findViewById(R.id.cameraButton);
        galleryImageButton = (Button) findViewById(R.id.galleryImageButton);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        uploadTextView = (TextView) findViewById(R.id.uploadImageTextView);
        nameTextView = (EditText) findViewById(R.id.editText);
        ocrResultText = (TextView) findViewById(R.id.ocrResultText);
        regButton = (Button) findViewById(R.id.regButton);
        scanImage = (Button) findViewById(R.id.scanImage);
        noResultText = (TextView) findViewById(R.id.noResultText);
        resultPage = (Button) findViewById(R.id.resultPage);
        mStorageRef = FirebaseStorage.getInstance().getReference();


        galleryImageButton.setText("글자 업로드");
        cameraButton.setText("글자 찍기");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);


        galleryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermission();
                sendTakePhotoIntent();
            }
        });


        resultPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToastText(ocrResult);
            }
        });
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToastText(lat  + "  " + lon);
                locname = nameTextView.getText().toString();
                postFirebaseDatabase();
            }
        });



    }

    // 이미지 불러오기
    private void getImageFromAlbum() {
        try {
            Intent pickPhotoIntent =
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            makeToastText("Gallery load failed");
        }
    }


    // 권한 설정
    private void askPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION_GRANTED);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    AddActivity.REQUEST_CAMERA_PERMISSION_GRANTED);
        }
    }

    // 아미지 불러오기
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_IMAGE_CAPTURE):
                    imageFilePath = imageFilePath2;
                    try {
                        image = getCorrectOrientedImage(imageFilePath);
                        makeToastText(imageFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        makeToastText(imageFilePath);
                    }


                    break;

                    /*
                    image = getCorrectOrientedImage(imageFilePath);
                    break;
                    */
                case (RESULT_LOAD_IMAGE):
                    Uri imageUri = data.getData();
                    imageFilePath = getRealPathFromURI(this, imageUri);

                    try {
                        image = getCorrectOrientedImage(imageFilePath);
                        makeToastText(imageFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        makeToastText(imageFilePath);
                    }

                    break;
            }
            if (accepted == 0)
                layoutProcess(image);
            else {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageBitmap(image);
                scanImage.setVisibility(View.INVISIBLE);
                checkandupload();
            }
        }
    }

    // 글자 인식 버튼
    private void layoutProcess(final Bitmap image) {

        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setImageBitmap(image);
        uploadTextView.setVisibility(View.GONE);
        scanImage.setVisibility(View.VISIBLE);
        ocrResultText.setVisibility(View.GONE);
        noResultText.setVisibility(View.GONE);
        resultPage.setVisibility(View.GONE);

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocrResult = textRecognition(image, AddActivity.this);
                ocrResultUserInterface(ocrResult);
            }
        });
    }

    //문자열 처리, 흡연구역인지 여부를 검사
    private void ocrResultUserInterface(String ocrResult) {
        scanImage.setVisibility(View.GONE);
        int len = ocrResult.length();
        if (len == 0) {
            noResultText.setVisibility(View.VISIBLE);
            accepted = 0;
            galleryImageButton.setText("글자 업로드");
            cameraButton.setText("글자 찍기");

        } else {
            {
                ocrResult.toUpperCase();
                int index = ocrResult.indexOf("SMOKING");
                int index2 = ocrResult.indexOf("AREA");
                int index3 = ocrResult.indexOf("구름다방");
                if(index>=0&&index2>=0) /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                {
                    new AlertDialog.Builder(AddActivity.this)
                            .setTitle("흡연구연 인증 완료")
                            .setMessage("흡연구역 인증이 완료되었습니다. \n위치를 쉽게 파악할 수 있도록 흡연구역 근처 배경 사진을 업로드해주세요.\n\n")
                            .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg, int sumthin) {

                                }
                            })
                            .show();
                    accepted = 1;

                    galleryImageButton.setText("배경사진 업로드");
                    cameraButton.setText("배경사진 찍기");
                    nameTextView.setVisibility(View.VISIBLE);
                    uploadTextView.setTextSize(14);
                    uploadTextView.setText("위치를 쉽게 파악할 수 있도록 배경 사진을 업로드해주세요.");
                    imagePreview.setVisibility(View.INVISIBLE);
                } else {
                    accepted = 0;
                    galleryImageButton.setText("글자 업로드");
                    cameraButton.setText("글자 찍기");
                    regButton.setVisibility(View.INVISIBLE);
                    nameTextView.setVisibility(View.INVISIBLE);
                }
            }
            resultPage.setVisibility(View.VISIBLE);
            uploadTextView.setVisibility(View.VISIBLE);

        }
    }

    // 토스트
    public void makeToastText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //데이터 업로드
    private void postFirebaseDatabase() {
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        FirebasePost post = new FirebasePost(lat,lon);
        postValues = post.toMap();

        childUpdates.put(locname, postValues);
        DBReference.child("locinfo").updateChildren(childUpdates);


        Uri file = Uri.fromFile(new File(imageFilePath));
        StorageReference riversRef = mStorageRef.child("images/" + locname);
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });

        new AlertDialog.Builder(AddActivity.this)
                .setTitle("등록 완료")
                .setMessage("등록이 완료되었습니다.\n\n")
                .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                        finish();
                    }
                })
                .show();
    }

    public void checkandupload() {
        if (accepted == 1 && imageFilePath != null) {
            regButton.setVisibility(View.VISIBLE);

            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddActivity.this, new String[]
                        {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, gpsLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, networkLocationListener);
            }
        }

    }


    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                pu = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pu);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
}