package alexademo.ellison.test.alexademo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.amazon.identity.auth.device.api.workflow.RequestContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import alexademo.ellison.test.alexademo.connect.ApiResponse;
import alexademo.ellison.test.alexademo.connect.AvsItem;
import alexademo.ellison.test.alexademo.connect.AvsSpeakItem;
import alexademo.ellison.test.alexademo.connect.AvsTemplateItem;
import alexademo.ellison.test.alexademo.connect.ConnectManager;
import alexademo.ellison.test.alexademo.ui.AboutDialog;
import alexademo.ellison.test.alexademo.ui.InfoTemplateView;
import alexademo.ellison.test.alexademo.ui.WeatherTemplateView;
import alexademo.ellison.test.alexademo.util.AudioPlayer;
import alexademo.ellison.test.alexademo.util.DateTimeUtil;
import alexademo.ellison.test.alexademo.util.LoginManager;
import ee.ioc.phon.android.speechutils.RawAudioRecorder;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getName();
    private RequestContext mRequestContext;
    private View mLoginButton, mPressButton, mPulseView, mProcessingView;

    private RawAudioRecorder mRecorder;
    private static final int AUDIO_RATE = 16000;
    private boolean isRecording = false;

    private ConnectManager mConnectManager;
    private InfoTemplateView mInfoTemplateView;
    private WeatherTemplateView mWeatherTemplateView;
    private AudioPlayer mAudioPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginManager.init(this);

        mAudioPlayer = new AudioPlayer(this);
        mRequestContext = RequestContext.create(this);
        mConnectManager = new ConnectManager(this);

        mWeatherTemplateView = findViewById(R.id.weather_template_view);
        mInfoTemplateView = findViewById(R.id.template_view);
        mLoginButton = findViewById(R.id.login_button);
        mPressButton = findViewById(R.id.press_button);
        mPulseView = findViewById(R.id.avi);
        mProcessingView = findViewById(R.id.processing_view);

        mLoginButton.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
        mPressButton.setVisibility(LoginManager.isLogin() ? View.VISIBLE : View.GONE);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.doLogin(mRequestContext, new LoginManager.LoginCallback() {
                    @Override
                    public void onSuccess() {
                        setLoginStatus();

                        long expireTime = LoginManager.getExpireTime();
                        Toast.makeText(MainActivity.this, "Login Success, Token Expires At " + DateTimeUtil.getDateString(expireTime), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(MainActivity.this, "Login Fail", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mPressButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //long press to listen
                        startListening();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        //release to stop recording and send request to alexa
                        stopListening();
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        setLoginStatus();
    }

    private void setLoginStatus() {
        //token expired
        if (LoginManager.isLogin() && System.currentTimeMillis() > LoginManager.getExpireTime()) {
            Toast.makeText(this, "Token Expired, Refreshing Token..", Toast.LENGTH_LONG).show();
            LoginManager.doRefreshToken(new LoginManager.LoginCallback() {
                @Override
                public void onSuccess() {
                    long expireTime = LoginManager.getExpireTime();
                    Toast.makeText(MainActivity.this, "Token Refreshed, Token Expires At " + DateTimeUtil.getDateString(expireTime), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFail() {
                    LoginManager.logout();
                    setLoginStatus();
                    Toast.makeText(MainActivity.this, "Refresh Token Failed", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        mLoginButton.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
        mPressButton.setVisibility(LoginManager.isLogin() ? View.VISIBLE : View.GONE);
    }

    private void startListening() {
        mPulseView.setVisibility(View.VISIBLE);
        if (!isRecording) {
            if (mRecorder == null) {
                mRecorder = new RawAudioRecorder(AUDIO_RATE);
            }
            mRecorder.start();
            isRecording = true;
        }
    }

    private void stopListening() {
        mPulseView.setVisibility(View.GONE);
        mProcessingView.setVisibility(View.VISIBLE);
        if (mRecorder != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final byte[] recordBytes = mRecorder.getCompleteRecording();

                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    isRecording = false;

                    mConnectManager.sendRequest(recordBytes, new ConnectManager.Callback() {
                        @Override
                        public void onResponse(final ApiResponse res) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (res.getResponseCode() != 200) {
                                        Toast.makeText(MainActivity.this, res.getResponseCode() + " " + res.getMessage(), Toast.LENGTH_LONG).show();
                                    } else {
                                        onAlexaResponse(res.getAvsItems());
                                    }
                                    mProcessingView.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            }).start();
        }
    }

    private void onAlexaResponse(List<AvsItem> res) {
        if (res != null) {
            for (AvsItem item : res) {
                if (item instanceof AvsSpeakItem) {
                    mAudioPlayer.play((AvsSpeakItem) item);

                }
                if (item instanceof AvsTemplateItem) {
                    final AvsTemplateItem templateItem = (AvsTemplateItem) item;
                    if (templateItem.isBodyType()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mInfoTemplateView.setVisibility(View.VISIBLE);
                                mInfoTemplateView.setData(templateItem);
                                Animation am = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_slide_in);
                                mInfoTemplateView.setAnimation(am);
                                am.startNow();
                            }
                        }, 100);
                    }
                    if (templateItem.isWeatherType()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mWeatherTemplateView.setVisibility(View.VISIBLE);
                                mWeatherTemplateView.setData(templateItem);
                                Animation am = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_slide_in);
                                mWeatherTemplateView.setAnimation(am);
                                am.startNow();
                            }
                        }, 100);
                    }

                }
            }
        }
    }

    private void checkAndRequestPermissions() {
        int permission1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRequestContext.onResume();
        checkAndRequestPermissions();
    }

    @Override
    public void onBackPressed() {
        if (mInfoTemplateView.getVisibility() == View.VISIBLE) {
            mInfoTemplateView.setVisibility(View.GONE);
            return;
        }
        if (mWeatherTemplateView.getVisibility() == View.VISIBLE) {
            mWeatherTemplateView.setVisibility(View.GONE);
            return;
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            new AboutDialog(MainActivity.this).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            trimCache();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void trimCache() {
        try {
            File dir = getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
