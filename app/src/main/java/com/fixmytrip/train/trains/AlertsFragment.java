package com.fixmytrip.train.trains;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fixmytrip.train.R;
import com.fixmytrip.train.utils.Constants;

/**
 * Created by philipkonieczny on 12/3/14.
 */
public class AlertsFragment extends Fragment{

    public AlertsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WebView rootView = (WebView) inflater.inflate(R.layout.activity_alerts, container, false);
        rootView.setWebViewClient(new WebViewClient());
        rootView.loadUrl(Constants.AlertsURL);
        return rootView;
    }
}
