package jp.techacademy.youichi.satou.autoslideshowapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.os.Handler;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.ActivityCompat;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "起動回数";
    private static final int PERMISSIONS_REQUEST_CODE = 5;
    private static int position;
    private static int last_position;
    private static int p_flag = 1;
    private static int count= 0;
    private int start_up = 0;

    Timer mTimer;
    Handler mHandler = new Handler();
    SharedPreferences mPreferences;
    ImageView mImageView;
    Uri ImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        count = mPreferences.getInt(TAG, 0);
        count++;
        if (count == 1) {
            start_up = 1;
        }
        Editor editor = mPreferences.edit();
        editor.putInt(TAG, count);
        editor.commit();

        Button button1 = (Button) findViewById(R.id.Button1);
        button1.setOnClickListener(this);

        Button button2 = (Button) findViewById(R.id.Button2);
        button2.setOnClickListener(this);

        Button button3 = (Button) findViewById(R.id.Button3);
        button3.setOnClickListener(this);

        // android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                // 許可されている
                getContentInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                showAlertDialog();
                start_up = 0;
                //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    p_flag = 1;
                    getContentInfo();
                } else {
                    p_flag = 0;
                }
                break;
            default:
                break;
        }
    }

    private void getContentInfo() {
        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);

            position = cursor.getPosition();

            cursor.moveToLast();
            last_position = cursor.getPosition();

        }
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.Button1) {
            if (p_flag == 1) {
                previousImage(position, last_position);
            } else {
                showAlertDialog();
            }
        } else if (v.getId() == R.id.Button2){
            if (p_flag == 1) {
                nextImage(position, last_position);
            } else {
                showAlertDialog();
            }
        } else if (v.getId() == R.id.Button3){
            if (p_flag == 1) {
                slideshow(position, last_position);
            } else {
                showAlertDialog();
            }
        }
    }

    private void nextImage(int p, int lp) {

        p += 1;
        if (p > lp) {
            p = 0;
        }

        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToPosition(p)) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);

            position = cursor.getPosition();

        }
        cursor.close();
    }

    private void previousImage(int p, int lp) {

        p -= 1;
        if (p < 0) {
            p = lp;
        }

        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToPosition(p)) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);

            position = cursor.getPosition();

        }
        cursor.close();
    }

    private void slideshow(int p, int lp) {

        p += 1;
        if (p > lp) {
            p = 0;
        }

        Button button1 = (Button) findViewById(R.id.Button1);
        Button button2 = (Button) findViewById(R.id.Button2);
        Button button3 = (Button) findViewById(R.id.Button3);
        String button_string = button3.getText().toString();

        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        position = p;
        last_position = lp;

        mImageView = (ImageView) findViewById(R.id.imageView);

        if (button_string.equals("再生")) {
            button3.setText("停止");
            button1.setEnabled(false);
            button2.setEnabled(false);
            if (mTimer == null) {
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (cursor.moveToNext()) {
                            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                            Long id = cursor.getLong(fieldIndex);
                            ImageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageURI(ImageUri);
                                position = cursor.getPosition();

                                if (position == last_position) {
                                    cursor.moveToPosition(-1);
                                    position = -1;
                                }
                            }
                        });
                    }
                }, 0, 2000);
            }
        } else if (button_string.equals("停止")) {
            position -= 1;
            button3.setText("再生");
            button1.setEnabled(true);
            button2.setEnabled(true);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) || (start_up == 1)){
            alertDialogBuilder.setTitle("許可をして下さい");
            alertDialogBuilder.setMessage("アプリケーションの機能を利用するには画像ファイルへのアクセスの許可が必要になります。" +
                    "\n次に表示されるダイアログの『許可』をタップして下さい");

            alertDialogBuilder.setPositiveButton("次へ",
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            alertDialogBuilder.setTitle("利用できません");
            alertDialogBuilder.setMessage("画像ファイルへのアクセスが許可されませんでしたので、アプリケーションの機能は利用できません。");
            alertDialogBuilder.setPositiveButton("閉じる",
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
