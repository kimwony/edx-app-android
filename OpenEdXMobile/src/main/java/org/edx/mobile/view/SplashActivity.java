package org.edx.mobile.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {
    protected final Logger logger = new Logger(getClass().getName());
    private Config config = new Config(MainApplication.instance());

    //파일 & 폴더 삭제
    public static void removeDir(String mRootPath) {
        File file = new File(mRootPath);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                removeDir(childFile.getAbsolutePath());    //하위 디렉토리
            }
            else {
                childFile.delete();    //하위 파일
            }
        }

        file.delete();    //root 삭제
    }

    public  void restarting() {

        Intent mStartActivity = new Intent(this, SplashActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            Context context = this;

            File FlagFile = new File(context.getExternalFilesDir(null).getParentFile().getParentFile() + "/com.nile.kmooc.db/flag");
//            File FlagFile = new File("/sdcard/flag");
            if (FlagFile.exists() == false) {
                String mRootPath = context.getExternalFilesDir(null).getParentFile().getAbsolutePath() + "/videos";
                File tmpDir = new File(mRootPath);
                if(tmpDir.isDirectory()) {
                    removeDir(mRootPath);
                }

                File to_path = context.getFilesDir().getParentFile();
                if(to_path.isDirectory()) {
                    removeDir(to_path.getAbsolutePath());
                }

                File dir = new File (context.getExternalFilesDir(null).getParentFile().getParentFile() + "/com.nile.kmooc.db");
                if (!dir.exists())
                {
                    dir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(FlagFile);

                new AlertDialog.Builder(this)
                        .setTitle(R.string.label_notification)
                        .setMessage(R.string.file_delete)
                        .setCancelable(false)
                        .setNeutralButton(R.string.label_ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                restarting();
                            }

                        })
                        .show();

            } else {
                if (!Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
                    finish();
                }

                /*
                Recommended solution to avoid opening of multiple tasks of our app's launcher activity.
                For more info:
                - https://issuetracker.google.com/issues/36907463
                - https://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
                - https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508#16447508
                 */
                if (!isTaskRoot()) {
                    final Intent intent = getIntent();
                    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                        return;
                    }
                }

                final IEdxEnvironment environment = MainApplication.getEnvironment(this);
                if (environment.getUserPrefs().getProfile() != null) {
                    environment.getRouter().showMyCourses(SplashActivity.this);
                } else if (!environment.getConfig().isRegistrationEnabled()) {
                    startActivity(environment.getRouter().getLogInIntent());
                } else {
                    environment.getRouter().showLaunchScreen(SplashActivity.this);
                }
            }
        } catch(Exception e){
            Log.d("version", "not first");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            final Branch branch = Branch.getInstance(getApplicationContext());
            branch.initSession(new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    if (error == null) {
                        // params are the deep linked params associated with the link that the user
                        // clicked -> was re-directed to this app params will be empty if no data found
                    } else {
                        logger.error(new Exception("Branch not configured properly, error:\n"
                                + error.getMessage()), true);
                    }
                }
            }, this.getIntent().getData(), this);

            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getInstance().closeSession();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
