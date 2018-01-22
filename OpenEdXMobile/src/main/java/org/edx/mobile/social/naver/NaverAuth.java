package org.edx.mobile.social.naver;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.edx.mobile.R;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.ISocialImpl;
import org.edx.mobile.view.LoginActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NaverAuth extends ISocialImpl {

    private static OAuthLogin mOAuthLoginInstance;
    private String accessToken;

    /**
     * client 정보를 넣어준다.
     */
    private static String OAUTH_CLIENT_ID;
    private static String OAUTH_CLIENT_SECRET;
    private static String OAUTH_CLIENT_NAME = "네이버 로그인";

    public static void setClientKeys(String id, String secret) {
        OAUTH_CLIENT_ID = id;
        OAUTH_CLIENT_SECRET = secret;
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    public NaverAuth(Activity activity) {
        super(activity);
    }

    @Override
    public void login() {
        Session session = Session.getActiveSession();
        if ( activity == null )
            return;
        if (session != null && !session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(activity)
                    .setPermissions(Arrays.asList("public_profile", "email"))
                    .setCallback(statusCallback));
        } else {
            //Session.openActiveSession(activity, true, Arrays.asList("public_profile", "email"), statusCallback);
            mOAuthLoginInstance = OAuthLogin.getInstance();

            mOAuthLoginInstance.init(activity, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

            mOAuthLoginInstance.startOauthLoginActivity(activity, mOAuthLoginHandler);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //uiHelper.onActivityResult(requestCode, resultCode, data);
//        System.out.println("******************start**********************");
//        System.out.println("1.requestCode :: '" + requestCode + "'");
//        System.out.println("2.resultCode :: '" + resultCode + "'");
//        System.out.println("3.data :: '" + data + "'");
//        System.out.println("******************end**********************");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);

    }

    @Override
    public void onActivityPaused(Activity activity) {
        super.onActivityPaused(activity);

    }

    @Override
    public void onActivityResumed(Activity activity) {
        super.onActivityResumed(activity);
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        super.onActivitySaveInstanceState(activity, outState);

    }

    private void onSessionStateChange(Session session, SessionState state,
            Exception exception) {
        if (state.isOpened()) {
            if (callback != null) {
                callback.onLogin(session.getAccessToken());
            }
            logger.debug("Naver Logged in...");
        } else if (state.isClosed()) {
            logger.debug("Naver Logged out...");
        } else {
            logger.debug("Naver state changed ...");
        }
    }

    @Override
    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
                //clear your preferences if saved
            }
        } else {
            if ( activity == null )
                return;
            session = new Session(activity);
            Session.setActiveSession(session);

            session.closeAndClearTokenInformation();
            //clear your preferences if saved
        }

        if(mOAuthLoginInstance != null){
            mOAuthLoginInstance.logout(activity.getBaseContext());
        }
        
        logger.debug("naver logged out");
    }

    /**
     * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는 걸 알 수 있다.
     */
    public OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                accessToken = mOAuthLoginInstance.getAccessToken(activity);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(activity);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(activity);
                String tokenType = mOAuthLoginInstance.getTokenType(activity);

                System.out.println("accessToken :: '" + accessToken + "'");
                new RequestApiTask().execute();

            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(activity).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(activity);
                //Toast.makeText(activity, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                System.out.println("errorCode :: '" + errorCode + "', errorDesc:'" + errorDesc + "'");
                callback.onLogin(null);
            }
        }

    };

    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginInstance.getAccessToken(activity);
            //mUserInfoMap = requestNaverUserInfo(mOAuthLoginInstance.requestApi(activity, at, url));
            return mOAuthLoginInstance.requestApi(activity, at, url);
        }

        protected void onPostExecute(String content) {
            super.onPostExecute(content);
            if (content != null && content instanceof String) {
                //accessToken = (String) content;
                logger.debug("Naver auth: accessToken: " + accessToken);
                if (callback != null) {
                    callback.onLogin(accessToken);
                }
            }
        }
    }

}
