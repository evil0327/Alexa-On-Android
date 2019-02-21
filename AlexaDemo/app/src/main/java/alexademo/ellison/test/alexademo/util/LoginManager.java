package alexademo.ellison.test.alexademo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import alexademo.ellison.test.alexademo.connect.ClientUtil;
import alexademo.ellison.test.alexademo.global.Constant;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginManager {
    private static final String TAG = LoginManager.class.getName();
    private static Context mContext;
    private static SharedPreferences mPref;
    private static final String KEY_TOKEN = "key_token";
    private final static String KEY_GRANT_TYPE = "grant_type";
    private final static String KEY_CODE = "code";
    private final static String KEY_REDIRECT_URI = "redirect_uri";
    private final static String KEY_CLIENT_ID = "client_id";
    private final static String KEY_CODE_VERIFIER = "code_verifier";
    private final static String KEY_REFRESH_TOKEN = "refresh_token";
    private final static String KEY_EXPIRE_TIME = "expire_time";

    private static Handler mHandler;

    public static void init(Context context) {
        mContext = context;
        mPref = context.getSharedPreferences("alexa_demo", Context.MODE_PRIVATE);
        mHandler = new Handler();
    }


    public static boolean isLogin() {
        return mPref.contains(KEY_TOKEN);
    }

    public static void logout() {
        mPref.edit().remove(KEY_TOKEN).apply();
    }

    public static String getToken() {
        return mPref.getString(KEY_TOKEN, "");
    }

    public static long getExpireTime(){
        return mPref.getLong(KEY_EXPIRE_TIME, 0);
    }

    public static void doLogin(RequestContext requestContext, final LoginCallback callback) {
        requestContext.registerListener(new AuthorizeListener() {
            /* Authorization was completed successfully. */
            @Override
            public void onSuccess(AuthorizeResult result) {
                final String authorizationCode = result.getAuthorizationCode();
                final String redirectUri = result.getRedirectURI();
                final String clientId = result.getClientId();

                mPref.edit().putString(KEY_CLIENT_ID, clientId).commit();

                doFetchAccessToken(authorizationCode, redirectUri, clientId, callback);
            }

            /* There was an error during the attempt to authorize the
            application. */
            @Override
            public void onError(AuthError ae) {
                Log.e(TAG, "onError result=" + ae.getMessage());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail();
                    }
                });
            }

            /* Authorization was cancelled before it could be completed. */
            @Override
            public void onCancel(AuthCancellation cancellation) {
                Log.e(TAG, "onCancel");
            }
        });

        final JSONObject scopeData = new JSONObject();
        final JSONObject productInstanceAttributes = new JSONObject();

        try {
            String android_id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            productInstanceAttributes.put("deviceSerialNumber", android_id);
            scopeData.put("productInstanceAttributes", productInstanceAttributes);
            scopeData.put("productID", Constant.PRODUCT_ID);

            AuthorizationManager.authorize(new AuthorizeRequest.Builder(requestContext)
                    .addScope(ScopeFactory.scopeNamed("alexa:all", scopeData))
                    .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
                    .withProofKeyParameters(getCodeChallenge(), "S256")
                    .shouldReturnUserData(false)
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void doRefreshToken(final LoginCallback callback){
        Log.e(TAG, "doRefreshToken");
        String url = "https://api.amazon.com/auth/O2/token";
        //set up our arguments for the api call, these will be the call headers
        FormBody.Builder builder = new FormBody.Builder()
                .add(KEY_GRANT_TYPE, "refresh_token")
                .add(KEY_REFRESH_TOKEN, mPref.getString(KEY_REFRESH_TOKEN, ""));
        builder.add(KEY_CLIENT_ID, mPref.getString(KEY_CLIENT_ID, ""));

        OkHttpClient client = ClientUtil.getTLS12OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                final TokenResponse tokenResponse = new Gson().fromJson(s, TokenResponse.class);

                Log.e(TAG, "doRefreshToken at="+tokenResponse.access_token);
                Log.e(TAG, "doRefreshToken rt="+tokenResponse.refresh_token);

                mPref.edit().putString(KEY_TOKEN, tokenResponse.access_token).commit();
                mPref.edit().putString(KEY_REFRESH_TOKEN, tokenResponse.refresh_token).commit();
                mPref.edit().putLong(KEY_EXPIRE_TIME, System.currentTimeMillis() + tokenResponse.expires_in*1000).commit();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                    }
                });
            }
        });
    }

    private static void doFetchAccessToken(String authorizationCode, String redirectUri, String clientId,  final LoginCallback callback) {
        //this url shouldn't be hardcoded, but it is, it's the Amazon auth access token endpoint
        String url = "https://api.amazon.com/auth/O2/token";

        //set up our arguments for the api call, these will be the call headers
        FormBody.Builder builder = new FormBody.Builder()
                .add(KEY_GRANT_TYPE, "authorization_code")
                .add(KEY_CODE, authorizationCode);
        builder.add(KEY_REDIRECT_URI, redirectUri);
        builder.add(KEY_CLIENT_ID, clientId);
        builder.add(KEY_CODE_VERIFIER, getCodeVerifier());

        OkHttpClient client = ClientUtil.getTLS12OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                final TokenResponse tokenResponse = new Gson().fromJson(s, TokenResponse.class);
                Log.e(TAG, "getAccessToken=" + tokenResponse.access_token + " ex=" + tokenResponse.expires_in);
                mPref.edit().putString(KEY_TOKEN, tokenResponse.access_token).commit();
                mPref.edit().putString(KEY_REFRESH_TOKEN, tokenResponse.refresh_token).commit();
                mPref.edit().putLong(KEY_EXPIRE_TIME, System.currentTimeMillis() + tokenResponse.expires_in*1000).commit();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                    }
                });
            }
        });
    }

    private static String getCodeVerifier() {
        if (mPref.contains(KEY_CODE_VERIFIER)) {
            return mPref.getString(KEY_CODE_VERIFIER, "");
        }
        String verifier = createCodeVerifier();
        mPref.edit().putString(KEY_CODE_VERIFIER, verifier).apply();
        return verifier;
    }

    static String createCodeVerifier() {
        return createCodeVerifier(128);
    }

    /**
     * Create a String hash based on the code verifier, this is used to verify the Token exchanges
     * @return
     */
    private static String getCodeChallenge(){
        String verifier = getCodeVerifier();
        return base64UrlEncode(getHash(verifier));
    }


    /**
     * Create a new code verifier for our token exchanges
     *
     * @return the new code verifier
     */
    static String createCodeVerifier(int count) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * Encode a byte array into a string, while trimming off the last characters, as required by the Amazon token server
     * <p>
     * See: http://brockallen.com/2014/10/17/base64url-encoding/
     *
     * @param arg our hashed string
     * @return a new Base64 encoded string based on the hashed string
     */
    private static String base64UrlEncode(byte[] arg) {
        String s = Base64.encodeToString(arg, 0); // Regular base64 encoder
        s = s.split("=")[0]; // Remove any trailing '='s
        s = s.replace('+', '-'); // 62nd char of encoding
        s = s.replace('/', '_'); // 63rd char of encoding
        return s;
    }

    /**
     * Hash a string based on the SHA-256 message digest
     *
     * @param password
     * @return
     */
    private static byte[] getHash(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public interface LoginCallback {
        void onSuccess();

        void onFail();
    }

    public static class TokenResponse {
        public String access_token;
        public String refresh_token;
        public String token_type;
        public long expires_in;
    }

}
