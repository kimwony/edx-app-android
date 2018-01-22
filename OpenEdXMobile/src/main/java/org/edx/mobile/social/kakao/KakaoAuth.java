package org.edx.mobile.social.kakao;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.ISocialImpl;
import org.edx.mobile.view.LoginActivity;
import org.edx.mobile.view.SplashActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KakaoAuth extends ISocialImpl {

    private String accessToken;
    private Fragment fragment;
    private android.support.v4.app.Fragment supportFragment;
    private SessionCallback callback1;

    public KakaoAuth(Activity activity) {
        super(activity);
    }

    @Override
    public void login() {
        Session session = Session.getCurrentSession();
        if ( activity == null )
            return;
        if (session != null && !session.isOpened() && !session.isClosed()) {
//            session.openForRead(new Session.OpenRequest(activity)
//                    .setPermissions(Arrays.asList("public_profile", "email"))
//                    .setCallback(statusCallback));
        } else {
            // 카톡 또는 카스가 존재하면 옵션을 보여주고, 존재하지 않으면 바로 직접 로그인창.
            final List<AuthType> authTypes = getAuthTypes();
            onClickLoginButton(authTypes);

            callback1 = new SessionCallback();
            Session.getCurrentSession().addCallback(callback1);
            Session.getCurrentSession().checkAndImplicitOpen();
        }
    }

    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList<>();
        if (Session.getAuthCodeManager().isTalkLoginAvailable()) {
            availableAuthTypes.add(AuthType.KAKAO_TALK);
        }
        if (Session.getAuthCodeManager().isStoryLoginAvailable()) {
            availableAuthTypes.add(AuthType.KAKAO_STORY);
        }
        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        AuthType[] authTypes = KakaoSDK.getAdapter().getSessionConfig().getAuthTypes();
        if (authTypes == null || authTypes.length == 0 || (authTypes.length == 1 && authTypes[0] == AuthType.KAKAO_LOGIN_ALL)) {
            authTypes = AuthType.values();
        }
        availableAuthTypes.retainAll(Arrays.asList(authTypes));

        // 개발자가 설정한 것과 available 한 타입이 없다면 직접계정 입력이 뜨도록 한다.
        if(availableAuthTypes.size() == 0){
            availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);
        }
        return availableAuthTypes;
    }

    private void onClickLoginButton(final List<AuthType> authTypes){
        if (authTypes.size() == 1) {
            openSession(authTypes.get(0));

        } else {
//            final LoginButton.Item[] authItems = createAuthItemArray(authTypes);
//            ListAdapter adapter = createLoginAdapter(authItems);
//            final Dialog dialog = createLoginDialog(authItems, adapter);
//            dialog.show();
        }
    }

    public void openSession(final AuthType authType) {
        if (getFragment() != null) {
            Session.getCurrentSession().open(authType, getFragment());
        } else if (getSupportFragment() != null) {
            Session.getCurrentSession().open(authType, getSupportFragment());
        } else {
            Session.getCurrentSession().open(authType, activity);
        }
    }

    public void setFragment(final Fragment fragment) {
        this.fragment = fragment;
    }

    public void setSuportFragment(final android.support.v4.app.Fragment fragment) {
        this.supportFragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    public android.support.v4.app.Fragment getSupportFragment() {
        return this.supportFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        System.out.println("^^^^^^^^^^^^^^^resultCode :: '" + resultCode + "', requestCode :: '" + requestCode + "'");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);

        Session.getCurrentSession().removeCallback(callback1);
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

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        super.onActivitySaveInstanceState(activity, outState);

    }

    private void onSessionStateChange(Session session, Exception exception) {

        if (session.isOpened()) {
            if (callback != null) {
                callback.onLogin(session.getAccessToken());
            }
            logger.debug("Kakao Logged in...");
        } else if (session.isClosed()) {
            logger.debug("Kakao Logged out...");
        } else {
            logger.debug("Kakao state changed ...");
        }
    }

    @Override
    public void logout() {
        Session session = Session.getCurrentSession();
        if (session != null) {
            if (!session.isClosed()) {

                Session.getCurrentSession().close();

            }
        } else {
            if ( activity == null )
                return;

            Session.getCurrentSession().close();
            //clear your preferences if saved
        }
        
        logger.debug("kakao logged out");
    }


    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            redirectSignupActivity();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
                redirectLogoutActivity();

            }

        }
    }

    protected void redirectSignupActivity() {

        callback.onLogin(Session.getCurrentSession().getAccessToken());

    }

    protected void redirectLogoutActivity() {

        //activity.getIntent().setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        callback.onLogin(null);
        //activity.startActivity(activity.getIntent());
    }
}
