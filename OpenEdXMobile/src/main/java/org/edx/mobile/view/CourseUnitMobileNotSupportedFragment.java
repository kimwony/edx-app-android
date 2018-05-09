package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.util.BrowserUtil;

import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {

    private TimerTask timerTask;
    private Timer timer;
//    public static final int MESSAGE_ID = 1;

//    Thread thread;
//    Handler handler;

    EdxCookieManager edxCookieManager = new EdxCookieManager();

//    final private int MESSAGE_ID = 100;

//    Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MESSAGE_ID: {
//                    edxCookieManager.flag = true;
//                }
//                break;
//                default:
//                    break;
//            }
//        }
//    };

//    //이벤트를 전달 받을 인터페이스
//    private EventListener eventListener;

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitMobileNotSupportedFragment newInstance(CourseComponent unit) {
        CourseUnitMobileNotSupportedFragment f = new CourseUnitMobileNotSupportedFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_grade, container, false);
        ((TextView) v.findViewById(R.id.not_available_message)).setText(
                unit.getType() == BlockType.VIDEO ? R.string.video_only_on_web_short : R.string.assessment_not_available);
        v.findViewById(R.id.view_on_web_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edxCookieManager.login_flag = false;
                edxCookieManager.setNullLoginCall();
                EdxCookieManager.getSharedInstance(getContext())
                        .tryToRefreshSessionCookie();
//                edxCookieManager.flag = false;
                //타이머 사용
                timer = new Timer(true);
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("start@@@@", "4");
                                if(edxCookieManager.login_flag == true) {
                                    Log.d("start@@@@", "1");
                                    environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
                                            unit.getDisplayName());
                                    timer.cancel();
                                    timer = null;
//                                    edxCookieManager.flag = false;
                                }
                                else {
                                    Log.d("start@@@@", "2");
                                }
                            }
                        });
                    }
//                    @Override
//                    public boolean cancel() {
//                        Log.d("cancel@@@@", "3");
//                        return super.cancel();
//                    }
                };
                timer.schedule(timerTask, 0, 100);
//                handler.sendEmptyMessage(0);

//                Message msg = new Message();
//                msg.what = 1;
//                environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                        unit.getDisplayName());

//                handler = new Handler();
//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                                        unit.getDisplayName());
//                            }
//                        });
//                    }
//                });
//                t.start();

//                flaglogin();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                                unit.getDisplayName());
//                        mHandler.sendEmptyMessage(100);
//                    }
//                }).start();

//                mHandler = new Handler();

//                mHandler = new Handler() {
//                    @Override
//                    public void handleMessage(Message msg) {
//                        if (msg.what == 0) {
//                            edxCookieManager.flag = true;
//                        }
//                    }
//                };

//                Thread t = new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                        mHandler.post(new Runnable(){
//                            @Override
//                            public void run() {
//                                if(edxCookieManager.flag) {
//                                    environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                                            unit.getDisplayName());
//                                }
//                            }
//                        });
//                    }
//                });
//                t.start();


//                if(edxCookieManager.flag == true) {
//                    environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                            unit.getDisplayName());
////                    edxCookieManager.flag = false;
//                }else {
//                    environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                            unit.getDisplayName());
//                    edxCookieManager.flag = true;
//                }

//                BrowserUtil.open(getActivity(), unit.getWebUrl());
//                environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId()
//                        , unit.getCourseId(), unit.isMultiDevice(), unit.getBlockId());
//                environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                        unit.getDisplayName());
//                EdxCookieManager.getSharedInstance(getContext())
//                        .tryToRefreshSessionCookie();

            }
        });
        return v;
}

//    public void flaglogin() {
//        if (edxCookieManager.flag == true) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
//                    environment.getRouter().showWebViewActivity(getActivity(), unit.getWebUrl(),
//                            unit.getDisplayName());
//                }
//            }, 100);
//        }else {
//            edxCookieManager.flag = true;
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            ViewPagerDownloadManager.instance.addTask(this);
    }


    @Override
    public void run() {
        ViewPagerDownloadManager.instance.done(this, true);
    }
}
