package com.example.dimas.recorder_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Manifest;

import android.app.Activity;
//import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;
import omrecorder.WriteAction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // проверка, вошел ли пользователь
    Boolean authorized = false;
    Boolean completeShift = false;
    String Name;
    String Password;
    int request_result = 0;
    String URL_path = "http://oasis-project.org:8192/";
    Uri uri;
    public String Token = "0";
    private SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_FLAG = "flag";
    public static final String APP_PREFERENCES_COMPLETE_SHIFT = "completeShift";
    public static final String APP_PREFERENCES_NAME = "name";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_TOKEN = "token";

    private  static  final int REQUEST_RECORD_AUDIO = 10001;
    Integer freq = 16000;

    ImageView recordButton;
    Button signOut;
    TextView text;
    Animation animation;
    Boolean recording;
    Recorder recorder;
    CheckBox skipSilence;


    private static final String RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupRecorder();

        if (!isPermissionGranted(RECORD_AUDIO_PERMISSION))
            requestMultiplePermissions();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        ConstraintLayout constraintLayout = findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(3000);
        animationDrawable.setExitFadeDuration(6000);
        animationDrawable.start();

        text = findViewById(R.id.mainText);
        signOut = findViewById(R.id.signOut);
        recordButton = findViewById(R.id.recordButton);
        animation = AnimationUtils.loadAnimation(this, R.anim.enlarge);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Thread recordThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            recording = true;
                            recorder.startRecording();
                        }

                    });
                    startAnim();
                    text.setText("Отпустите кнопку \n для окончания записи");
                    signOut.setEnabled(false);
                    recordThread.start();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    recording = false;
                    try {
                        recorder.stopRecording();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    view.performClick();
                    clearAnim();
                    text.setText("Обработка записи...");
                    recordButton.setEnabled(false);
                    Upload();
                }
                return true;
            }
        });
    }

    void startAnim(){ recordButton.startAnimation(animation); }

    void clearAnim() {recordButton.clearAnimation();}

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode > 0){
            Name = data.getStringExtra(LoginActivity.Name);
            Password = data.getStringExtra(LoginActivity.Password);
            authorized = true;
            completeShift = true;

            String var = "";
            if (resultCode == 1)
                var = "login";
            if (resultCode == 2)
                var = "register";

            try {
                RegistrationBody body = new RegistrationBody(URLEncoder.encode(Name, "UTF-8"),
                        URLEncoder.encode(Password, "UTF-8"), "ru_RU.UTF-8");
                AppServer.getApi().registerUser(var, body).enqueue(new Callback<RegistrationResponse>() {
                    @Override
                    public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                        if (response.code() == 200) {
                            Token = response.body().token;
                            if (Token == null){
                                Token = "0";
                                Toast.makeText(MainActivity.this, "Неверное имя или пароль", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivityForResult(intent, request_result);
                            }
                            if (resultCode == 2)
                                Toast.makeText(MainActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                            else if (resultCode == 1)
                                Toast.makeText(MainActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(MainActivity.this, "Неполадки с сервером, подождите", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<RegistrationResponse> call, Throwable throwable) {
                        Toast.makeText(MainActivity.this, "Сервер не отвечает", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            putValues();
        }
        else {
            finish();
        }
    }

    // проверка: получены ли разрешения
    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        // true - если есть, false - если нет
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.CHANGE_NETWORK_STATE,
                        android.Manifest.permission.RECEIVE_BOOT_COMPLETED
                },
                REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // проверка по запрашиваемому коду
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!authorized) {
                    authorized = true;
                    SharedPreferences.Editor editor_a = mSettings.edit();
                    editor_a.putBoolean(APP_PREFERENCES_FLAG, authorized);
                    editor_a.apply();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, request_result);
                }
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // upload

    public void Upload(){
        final File file = new File(Environment.getExternalStorageDirectory(),"rec.pcm");

        if (Token.equals("0")) {

            Token = "91e848e57aa36128ec1815047ea40c1186dd215ecae0aea103ab74adf497ad4a7e85ae2a666776e6dbf07a9ea062295ee11164aa34814112ab90f72a66b1d282";
        }

        final RequestBody requestFile = RequestBody.create(MediaType.parse("audio/x-pcm"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("multipart/form-data",
                file.getName(), requestFile);

        String descriptionString = Token;
        RequestBody description = RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        AppServer.getApi().search_audio(description, body).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.code() == 200) {
                    try{
                        String search_text = response.body().author + "\n" + response.body().name + "\n"
                                + response.body().genre;  // + "\n" + response.body().urls;
                        if (response.body().name == null)
                            text.setText("Песня не найдена");
                        else {
                            text.setText(search_text);
                        }
                        recordButton.setEnabled(true);
                        signOut.setEnabled(true);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Неполадки с сервером, подождите", Toast.LENGTH_SHORT).show();
                    text.setText("Нажмите и удерживайте \n кнопку для записи");
                    recordButton.setEnabled(true);
                    signOut.setEnabled(true);
                }
                setupRecorder();
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable throwable) {
                if (throwable.getMessage().equals("End of input at line 1 column 1 path $"))
                    text.setText("Песня не найдена");
                else{
                    Toast.makeText(MainActivity.this, "Сервер не отвечает", Toast.LENGTH_SHORT).show();
                    text.setText("Нажмите и удерживайте \n кнопку для записи");
                }
                recordButton.setEnabled(true);
                signOut.setEnabled(true);
                setupRecorder();
            }
        });
    }


    protected void onPause(){
        super.onPause();
        if (completeShift) {
            putValues();
        }
    }

    protected void putValues(){
        SharedPreferences.Editor editor_a = mSettings.edit();
        SharedPreferences.Editor editor_c = mSettings.edit();
        SharedPreferences.Editor editor_n = mSettings.edit();
        SharedPreferences.Editor editor_p = mSettings.edit();
        SharedPreferences.Editor editor_t = mSettings.edit();
        editor_a.putBoolean(APP_PREFERENCES_FLAG, authorized);
        editor_c.putBoolean(APP_PREFERENCES_COMPLETE_SHIFT, completeShift);
        editor_n.putString(APP_PREFERENCES_NAME, Name);
        editor_p.putString(APP_PREFERENCES_PASSWORD, Password);
        editor_t.putString(APP_PREFERENCES_TOKEN, Token);
        editor_a.apply();
        editor_c.apply();
        editor_n.apply();
        editor_p.apply();
        editor_t.apply();
    }

    protected void onResume() {
        super.onResume();
        if (mSettings.contains(APP_PREFERENCES_FLAG) && mSettings.contains(APP_PREFERENCES_COMPLETE_SHIFT)) {
            completeShift = mSettings.getBoolean(APP_PREFERENCES_COMPLETE_SHIFT, false);
            if (completeShift) {
                authorized = mSettings.getBoolean(APP_PREFERENCES_FLAG, false);
                Name = mSettings.getString(APP_PREFERENCES_NAME, "noName");
                Password = mSettings.getString(APP_PREFERENCES_PASSWORD, "noPassword");
                Token = mSettings.getString(APP_PREFERENCES_TOKEN, "0");
            }
        }
        // если авторизация не произведена
        if (isPermissionGranted(RECORD_AUDIO_PERMISSION) && !authorized){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, request_result);
        }
    }


    public void OnClickSignOut(View view){
            authorized = false;
            completeShift = false;
            SharedPreferences.Editor editor_a = mSettings.edit();
            SharedPreferences.Editor editor_c = mSettings.edit();
            editor_a.putBoolean(APP_PREFERENCES_FLAG, authorized);
            editor_c.putBoolean(APP_PREFERENCES_COMPLETE_SHIFT, completeShift);
            editor_a.apply();
            editor_c.apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, request_result);
    }


    private void setupRecorder() {
        recorder = OmRecorder.pcm(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }


    private void animateVoice(final float maxPeak) {
        recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 16000
                )
        );
    }

    @NonNull private File file() {
        return new File(Environment.getExternalStorageDirectory(), "rec.pcm");
    }
}

