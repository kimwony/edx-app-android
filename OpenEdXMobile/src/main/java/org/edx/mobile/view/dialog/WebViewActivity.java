package org.edx.mobile.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class WebViewActivity extends BaseFragmentActivity {

    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    WebView webView;

    public static Intent newIntent(@NonNull Context context, @NonNull String url, @Nullable String title) {
        return new Intent(context, WebViewActivity.class)
                .putExtra(ARG_URL, url)
                .putExtra(ARG_TITLE, title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final ProgressBar progress = (ProgressBar) findViewById(R.id.loading_indicator);
        progress.setVisibility(View.GONE);

        webView = (WebView) findViewById(R.id.webView);
//        webView.setPadding(0,0,0,0);

        final URLInterceptorWebViewClient client =
                new URLInterceptorWebViewClient(this, webView);
//        WebSettings set = webView.getSettings();

        webView.setInitialScale(getScale());
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);


        client.setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {

            @Override
            public void onPageStarted() {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse,
                                        boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPagePartiallyLoaded() {
                progress.setVisibility(View.GONE);
            }
        });


        webView.loadUrl(getIntent().getStringExtra(ARG_URL));

        final String title = getIntent().getStringExtra(ARG_TITLE);
        if (!TextUtils.isEmpty(title)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(title);
        }
    }

    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == newConfig.ORIENTATION_LANDSCAPE)
        {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(false);
//            findViewById(R.id.course_unit_nav_bar).setVisibility(View.GONE);
        }else {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(true);
//            findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webView.destroy();
    }

    private int getScale(){
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/800d;
        val = val * 100d;
        return val.intValue();
    }
}
