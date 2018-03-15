package org.edx.mobile.social.kakao;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;

import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.edx.mobile.R;
import org.edx.mobile.social.ISocialImpl;
import org.edx.mobile.view.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KakaoAuth extends ISocialImpl {

    private String accessToken;
    private Fragment fragment;
    private android.support.v4.app.Fragment supportFragment;
    private SessionCallback callback1;

    Context mContext;

    public KakaoAuth(Activity activity) {
        super(activity);
        mContext = this.activity;
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

            final Item[] authItems = createAuthItemArray(authTypes);
            ListAdapter adapter = createLoginAdapter(authItems);
            final Dialog dialog = createLoginDialog(authItems, adapter);
            dialog.show();
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

    /**
     * 가능한 AuhType들이 담겨 있는 리스트를 인자로 받아 로그인 어댑터의 data source로 사용될 Item array를 반환한다.
     * @param authTypes 가능한 AuthType들을 담고 있는 리스트
     * @return 실제로 로그인 방법 리스트에 사용될 Item array
     */
    private Item[] createAuthItemArray(final List<AuthType> authTypes) {
        final List<Item> itemList = new ArrayList<Item>();
        if(authTypes.contains(AuthType.KAKAO_TALK)) {
            itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account, com.kakao.usermgmt.R.drawable.talk, com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account_tts, AuthType.KAKAO_TALK));
        }
        if(authTypes.contains(AuthType.KAKAO_STORY)) {
            itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_kakaostory_account, com.kakao.usermgmt.R.drawable.story, com.kakao.usermgmt.R.string.com_kakao_kakaostory_account_tts, AuthType.KAKAO_STORY));
        }
        if(authTypes.contains(AuthType.KAKAO_ACCOUNT)){
            itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount, com.kakao.usermgmt.R.drawable.account, com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount_tts, AuthType.KAKAO_ACCOUNT));
        }

        return itemList.toArray(new Item[itemList.size()]);
    }

    @SuppressWarnings("deprecation")
    private ListAdapter createLoginAdapter(final Item[] authItems) {
        /**
         * 가능한 auth type들을 유저에게 보여주기 위한 준비.
         */
        return new ArrayAdapter<Item>(
                mContext,
                android.R.layout.select_dialog_item,
                android.R.id.text1, authItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(com.kakao.usermgmt.R.layout.layout_login_item, parent, false);
                }
                ImageView imageView = (ImageView) convertView.findViewById(com.kakao.usermgmt.R.id.login_method_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(activity.getResources().getDrawable(authItems[position].icon, getContext().getTheme()));
                } else {
                    imageView.setImageDrawable(activity.getResources().getDrawable(authItems[position].icon));
                }
                TextView textView = (TextView) convertView.findViewById(com.kakao.usermgmt.R.id.login_method_text);
                textView.setText(authItems[position].textId);
                return convertView;
            }
        };
    }

    /**
     * 실제로 유저에게 보여질 dialog 객체를 생성한다.
     * @param authItems 가능한 AuthType들의 정보를 담고 있는 Item array
     * @param adapter Dialog의 list view에 쓰일 adapter
     * @return 로그인 방법들을 팝업으로 보여줄 dialog
     */
    private Dialog createLoginDialog(final Item[] authItems, final ListAdapter adapter) {
        final Dialog dialog = new Dialog(mContext, com.kakao.usermgmt.R.style.LoginDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(com.kakao.usermgmt.R.layout.layout_login_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
//        TextView textView = (TextView) dialog.findViewById(R.id.login_title_text);
//        Typeface customFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/KakaoOTFRegular.otf");
//        if (customFont != null) {
//            textView.setTypeface(customFont);
//        }

        ListView listView = (ListView) dialog.findViewById(com.kakao.usermgmt.R.id.login_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AuthType authType = authItems[position].authType;
                if (authType != null) {
                    openSession(authType);
                }
                dialog.dismiss();
            }
        });

        Button closeButton = (Button) dialog.findViewById(com.kakao.usermgmt.R.id.login_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final Intent launchIntent = new Intent(mContext, LoginActivity.class);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(launchIntent);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
               @Override
               public void onCancel(DialogInterface dialog) {
                   dialog.dismiss();

                   final Intent launchIntent = new Intent(mContext, LoginActivity.class);
                   launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                   mContext.startActivity(launchIntent);
               }
        });
        return dialog;
    }

    /**
     * 각 로그인 방법들의 text, icon, 실제 AuthTYpe들을 담고 있는 container class.
     */
    private static class Item {
        final int textId;
        public final int icon;
        final int contentDescId;
        final AuthType authType;
        Item(final int textId, final Integer icon, final int contentDescId, final AuthType authType) {
            this.textId = textId;
            this.icon = icon;
            this.contentDescId = contentDescId;
            this.authType = authType;
        }
    }
}
