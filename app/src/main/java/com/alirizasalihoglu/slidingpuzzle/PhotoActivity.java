package com.alirizasalihoglu.slidingpuzzle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoActivity extends AppCompatActivity {

    private Button cameraButton, galleryButton, goButton;
    private ImageView imageView;
    Uri loadedUri;
    int TAKE_PHOTO = 15;
    int SELECT_FILE = 16;
    String selectedImageTag = null;
    boolean imageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        galleryButton = (Button) findViewById(R.id.galleryButton);
        goButton = (Button) findViewById(R.id.goButton);

        imageView = (ImageView) findViewById(R.id.imageView4);
        imageView.setTag("Custom Image");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            galleryButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        goButton.setEnabled(false);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraButton.setEnabled(true);
            }
        }
        else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galleryButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),loadedUri);
                    bitmap = Bitmap.createScaledBitmap(bitmap, imageView.getLayoutParams().width, imageView.getLayoutParams().height, true);
                    imageView.setImageBitmap(bitmap);
                    if(!imageLoaded){
                        imageLoaded = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == SELECT_FILE) {
                Bitmap bitmap = null;
                if (data != null) {
                    try {
                        loadedUri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), loadedUri);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bitmap = Bitmap.createScaledBitmap(bitmap,imageView.getLayoutParams().width,imageView.getLayoutParams().height, true);

                imageView.setImageBitmap(bitmap);
                if(!imageLoaded){
                    imageLoaded = true;
                }
            }
        }

    }
    public void clickGo(View view){
        Intent goIntent = new Intent(getApplicationContext(), GameActivity.class);
        goIntent.putExtra("Image Tag", selectedImageTag);

        if(selectedImageTag.equals("Custom Image")){
            goIntent.setData(loadedUri);
        }

        startActivity(goIntent);
    }

    public void clickCamera(View view){

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        loadedUri = FileProvider.getUriForFile(getApplicationContext(),
                getApplicationContext().getApplicationContext().getPackageName() + ".com.alirizasalihoglu.slidingpuzzle.provider",
                getOutputMediaFile());
        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, loadedUri);
        startActivityForResult(takePhotoIntent, TAKE_PHOTO);
    }

    public void clickGallery(View view){

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select File"),SELECT_FILE);

    }

    public void clickImage(View view){

        ViewGroup parent = (ViewGroup ) view.getParent();

        if(view.getTag() != null && view.getTag().equals("Custom Image") && imageLoaded == false) {
            return;
        }

        for(int i = 0; i < parent.getChildCount(); i++) {
            View nextChild = parent.getChildAt(i);
            nextChild.setSelected(false);
        }
        selectedImageTag = view.getTag().toString();
        if(!goButton.isEnabled()){
            goButton.setEnabled(true);
        }
        view.setSelected(true);


    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SlidingPuzzlePhotos");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }
}
