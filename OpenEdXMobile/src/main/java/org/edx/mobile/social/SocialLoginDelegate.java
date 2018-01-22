package org.edx.mobile.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.social.google.GoogleOauth2;
import org.edx.mobile.social.google.GoogleProvider;
import org.edx.mobile.social.kakao.KakaoProvider;
import org.edx.mobile.social.naver.NaverProvider;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.ICommonUI;

import java.util.HashMap;


/**
 * Code refactored from Login Activity, for the logic of login to social site are the same
 * for both login and registration.
 */
public class SocialLoginDelegate {

    protected final Logger logger = new Logger(getClass().getName());

    private Activity activity;
    private MobileLoginCallback callback;
    private ISocial google, facebook, naver, kakao;
    private final LoginPrefs loginPrefs;

    private String userEmail;

    private ISocial.Callback googleCallback = new ISocial.Callback() {
        @Override
        public void onLogin(String accessToken) {
            logger.debug("Google logged in; token= " + accessToken);
            onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_GOOGLE);
        }

    };

    private ISocial.Callback facebookCallback = new ISocial.Callback() {

        @Override
        public void onLogin(String accessToken) {
            logger.debug("Facebook logged in; token= " + accessToken);
            onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_FACEBOOK);
        }
    };

    private ISocial.Callback naverCallback = new ISocial.Callback() {

        @Override
        public void onLogin(String accessToken) {
            logger.debug("Naver logged in; token= " + accessToken);
            if (accessToken == null){
                callback.onUserLoginFailure(null, null, PrefManager.Value.BACKEND_NAVER);
            } else {
                onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_NAVER);
            }

        }
    };

    private ISocial.Callback kakaoCallback = new ISocial.Callback() {

        @Override
        public void onLogin(String accessToken) {
            logger.debug("Kakao logged in; token= " + accessToken);
            if (accessToken == null){
                callback.onUserLoginFailure(null, null, PrefManager.Value.BACKEND_KAKAO);
            } else {
                onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_KAKAO);
            }

        }
    };


    public SocialLoginDelegate(Activity activity, Bundle savedInstanceState, MobileLoginCallback callback, Config config, LoginPrefs loginPrefs) {

        this.activity = activity;
        this.callback = callback;
        this.loginPrefs = loginPrefs;

        google = SocialFactory.getInstance(activity, SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE, config);
        google.setCallback(googleCallback);

        facebook = SocialFactory.getInstance(activity, SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK, config);
        facebook.setCallback(facebookCallback);

        naver = SocialFactory.getInstance(activity, SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_NAVER, config);
        naver.setCallback(naverCallback);

        kakao = SocialFactory.getInstance(activity, SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_KAKAO, config);
        kakao.setCallback(kakaoCallback);

        google.onActivityCreated(activity, savedInstanceState);
        facebook.onActivityCreated(activity, savedInstanceState);
        naver.onActivityCreated(activity, savedInstanceState);
        kakao.onActivityCreated(activity, savedInstanceState);
    }

    public void onActivityDestroyed() {
        google.onActivityDestroyed(activity);
        facebook.onActivityDestroyed(activity);
        naver.onActivityDestroyed(activity);
        kakao.onActivityDestroyed(activity);
    }

    public void onActivitySaveInstanceState(Bundle outState) {
        google.onActivitySaveInstanceState(activity, outState);
        facebook.onActivitySaveInstanceState(activity, outState);
        naver.onActivitySaveInstanceState(activity, outState);
        kakao.onActivitySaveInstanceState(activity, outState);
    }

    public void onActivityStarted() {
        google.onActivityStarted(activity);
        facebook.onActivityStarted(activity);
        naver.onActivityStarted(activity);
        kakao.onActivityStarted(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        google.onActivityResult(requestCode, resultCode, data);
        facebook.onActivityResult(requestCode, resultCode, data);
        naver.onActivityResult(requestCode, resultCode, data);
        kakao.onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityStopped() {
        google.onActivityStopped(activity);
        facebook.onActivityStopped(activity);
        naver.onActivityStopped(activity);
        kakao.onActivityStopped(activity);
    }

    public void socialLogin(SocialFactory.SOCIAL_SOURCE_TYPE socialType) {
        if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK)
            facebook.login();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE)
            google.login();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_NAVER)
            naver.login();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_KAKAO)
            kakao.login();
    }

    public void socialLogout(SocialFactory.SOCIAL_SOURCE_TYPE socialType) {
        if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK)
            facebook.logout();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE)
            google.logout();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_NAVER)
            naver.logout();
        else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_KAKAO)
            kakao.logout();
    }

    /**
     * called with you to use social login
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend) {
        loginPrefs.saveSocialLoginToken(accessToken, backend);
        Task<?> task = new ProfileTask(activity, accessToken, backend);
        callback.onSocialLoginSuccess(accessToken, backend, task);
        task.execute();
    }


    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserEmail() {
        return this.userEmail;
    }


    public void getUserInfo(SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken, final SocialUserInfoCallback userInfoCallback) {
        SocialProvider socialProvider = null;
        if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK) {
            socialProvider = new FacebookProvider();
        } else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE) {
            socialProvider = new GoogleProvider((GoogleOauth2) google);
        } else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_NAVER) {
            socialProvider = new NaverProvider();
        } else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_KAKAO) {
            socialProvider = new KakaoProvider();
        }

        if (socialProvider != null) {
            socialProvider.getUserInfo(activity, socialType, accessToken, userInfoCallback);
        }

    }


    private class ProfileTask extends Task<ProfileModel> {

        private String accessToken;
        private String backend;

        @Inject
        LoginAPI loginAPI;

        public ProfileTask(Context context, String accessToken, String backend) {
            super(context);
            this.accessToken = accessToken;
            this.backend = backend;
        }

        @Override
        public void onSuccess(ProfileModel result) {
            callback.onUserLoginSuccess(result);
        }

        @Override
        public void onException(Exception ex) {
            super.onException(ex);
            callback.onUserLoginFailure(ex, this.accessToken, this.backend);
        }

        @Override
        public ProfileModel call() throws Exception {
            final AuthResponse auth;
            final HashMap<String, CharSequence> descParams = new HashMap<>();
            descParams.put("platform_name", environment.getConfig().getPlatformName());
            descParams.put("platform_destination", environment.getConfig().getPlatformDestinationName());
            if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_FACEBOOK)) {
                try {
                    auth = loginAPI.logInUsingFacebook(accessToken);
                } catch (LoginAPI.AccountNotLinkedException e) {
                    CharSequence title = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_title_fb, descParams);
                    CharSequence desc = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_desc_fb, descParams);
                    throw new LoginException(new LoginErrorMessage(title.toString(), desc.toString()));
                }
            } else if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_GOOGLE)) {
                try {
                    auth = loginAPI.logInUsingGoogle(accessToken);
                } catch (LoginAPI.AccountNotLinkedException e) {
                    CharSequence title = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_title_google, descParams);
                    CharSequence desc = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_desc_google, descParams);
                    throw new LoginException(new LoginErrorMessage(title.toString(), desc.toString()));
                }
            } else if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_NAVER)) {
                try {
                    auth = loginAPI.logInUsingNaver(accessToken);
                } catch (LoginAPI.AccountNotLinkedException e) {
                    CharSequence title = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_title_naver, descParams);
                    CharSequence desc = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_desc_naver, descParams);
                    throw new LoginException(new LoginErrorMessage(title.toString(), desc.toString()));
                }
            } else if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_KAKAO)) {
                try {
                    auth = loginAPI.logInUsingKakao(accessToken);
                } catch (LoginAPI.AccountNotLinkedException e) {
                    CharSequence title = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_title_kakao, descParams);
                    CharSequence desc = ResourceUtil.getFormattedString(context.getResources(), R.string.error_account_not_linked_desc_kakao, descParams);
                    throw new LoginException(new LoginErrorMessage(title.toString(), desc.toString()));
                }
            } else {
                throw new IllegalArgumentException("Unknown backend: " + backend);
            }
            return auth.profile;
        }

    }

    public SocialButtonClickHandler createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE socialType) {
        return new SocialButtonClickHandler(socialType);
    }

    public class SocialButtonClickHandler implements View.OnClickListener {
        private SocialFactory.SOCIAL_SOURCE_TYPE socialType;

        private SocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE socialType) {
            this.socialType = socialType;
        }

        @Override
        public void onClick(View v) {
            if (!NetworkUtil.isConnected(activity)) {
                callback.showAlertDialog(activity.getString(R.string.no_connectivity),
                        activity.getString(R.string.network_not_connected));
            } else {
                Task<Void> logout = new Task<Void>(activity) {

                    @Override
                    public Void call() {
                        socialLogout(socialType);
                        return null;
                    }

                    @Override
                    public void onSuccess(Void result) {
                        socialLogin(socialType);
                    }

                    @Override
                    public void onException(Exception ex) {
                        super.onException(ex);
                        if (activity instanceof ICommonUI)
                            ((ICommonUI) activity).tryToSetUIInteraction(true);
                    }
                };
                if (activity instanceof ICommonUI)
                    ((ICommonUI) activity).tryToSetUIInteraction(false);
                logout.execute();
            }
        }
    }


    public interface MobileLoginCallback {
        void onSocialLoginSuccess(String accessToken, String backend, Task task);

        void onUserLoginFailure(Exception ex, String accessToken, String backend);

        void onUserLoginSuccess(ProfileModel profile);

        void showAlertDialog(String header, String message);
    }

    public interface SocialUserInfoCallback {
        void setSocialUserInfo(String email, String name);
    }

}
