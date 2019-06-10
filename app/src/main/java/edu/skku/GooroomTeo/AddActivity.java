package edu.skku.GooroomTeo;

import android.Manifest;
import android.content.Context;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    private Button cameraButton, uploadImageButton;
    private ImageView imagePreview;
    private TextView uploadTextView, ocrResultText, noResultText;
    private EditText nameTextView;
    private Button regButton, scanImage, resultPage;
    private Bitmap image;
    private static String imageFilePath;
    private DatabaseReference DBReference = FirebaseDatabase.getInstance().getReference();

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
            makeToastText(provider + " " + lon + " " + lat);
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

    // 이미지 파일 생성
    static File createImageFile() throws IOException {
        // uses timestamp to generate a unique filename everytime a file is created
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";

        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //OCR(글자 인식)
    static String texRecognition(Bitmap image, Context context) {
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

    //Path 구하기
    static String getRealPathFromURI(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        cameraButton = (Button) findViewById(R.id.cameraButton);
        uploadImageButton = (Button) findViewById(R.id.galleryImageButton);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        uploadTextView = (TextView) findViewById(R.id.uploadImageTextView);
        nameTextView = (EditText) findViewById(R.id.editText);
        ocrResultText = (TextView) findViewById(R.id.ocrResultText);
        regButton = (Button) findViewById(R.id.regButton);
        scanImage = (Button) findViewById(R.id.scanImage);
        noResultText = (TextView) findViewById(R.id.noResultText);
        resultPage = (Button) findViewById(R.id.resultPage);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermission();
                dispatchTakePictureIntent();
            }
        });


        resultPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToastText(ocrResultText.getText().toString());
            }
        });
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToastText(lon + "  " + lat);
                locname = nameTextView.getText().toString();
                postFirebaseDatabase();
            }
        });



    }

    // 이미지 불러오기
    private void getImageFromAlbum() {
        try {
            // make sure that the intent only allows choosing images.
            Intent pickPhotoIntent =
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            makeToastText("Gallery load failed");
        }
    }

    // 카메라로 사진 찍기
    private void dispatchTakePictureIntent() {
        try {
            // initialize the intent and photo file to be created
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;

            try {
                photoFile = createImageFile();
                imageFilePath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                makeToastText(imageFilePath);
            }

            String authorities = getApplicationContext().getPackageName() + ".provider";
            Uri cameraIntentUri = FileProvider.getUriForFile(AddActivity.this, authorities, photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraIntentUri);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception e) {
            makeToastText("Camera load failed");
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
                    image = getCorrectOrientedImage(imageFilePath);
                    break;
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
            layoutProcess(image);
        }
    }

    // 글자 인식 버튼
    private void layoutProcess(final Bitmap image) {
        imagePreview.setImageBitmap(image);
        uploadTextView.setVisibility(View.GONE);
        scanImage.setVisibility(View.VISIBLE);
        ocrResultText.setVisibility(View.GONE);
        noResultText.setVisibility(View.GONE);
        resultPage.setVisibility(View.GONE);

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ocrResult = texRecognition(image, AddActivity.this);
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
        } else {
            for (int i = 0; i < len; i++) {
                ocrResult.toUpperCase();
                int index = ocrResult.indexOf("SMOKING");
                int index2 = ocrResult.indexOf("AREA");
                int index3 = ocrResult.indexOf("구름다방");
                if(index>=0&&index2>=0) /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                {
                    makeToastText("accepted");
                    regButton.setVisibility(View.VISIBLE);
                    nameTextView.setVisibility(View.VISIBLE);
                    final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    if ( Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission( getApplicationContext(),
                                    android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions( AddActivity.this, new String[]
                                { android.Manifest.permission.ACCESS_FINE_LOCATION },0 );
                    } else {
                        Toast.makeText(AddActivity.this, "LocationManager is ready!", Toast.LENGTH_SHORT).show();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                100,
                                0,
                                gpsLocationListener);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                100,
                                0,
                                networkLocationListener);
                    }
                } else {
                    regButton.setVisibility(View.INVISIBLE);
                    nameTextView.setVisibility(View.INVISIBLE);
                }
            }
            resultPage.setVisibility(View.VISIBLE);
            ocrResultText.setText(ocrResult);
            uploadTextView.setVisibility(View.VISIBLE);
            uploadTextView.setTextSize(18);
            uploadTextView.setText(ocrResult);
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
        FirebasePost post = new FirebasePost(lon,lat);
        postValues = post.toMap();

        childUpdates.put(locname, postValues);
        DBReference.updateChildren(childUpdates);
    }
}