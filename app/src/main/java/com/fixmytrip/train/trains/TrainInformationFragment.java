package com.fixmytrip.train.trains;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fixmytrip.train.R;
import com.fixmytrip.train.utils.Constants;

/**
 * Created by philipkonieczny on 3/7/15.
 */
public class TrainInformationFragment extends Fragment {
    public TrainInformationFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WebView rootView = (WebView) inflater.inflate(R.layout.activity_train_info
                , container, false);
        String train=null;
        try
        {
            train= getArguments().getString("Train");
        }
        catch(Exception e)
        {
            //do nothing    
        }
        
        String URL = Constants.TrainInfoURL;
        if(train!=null)
        {
            URL+= "lines/subway/" + train;
        }
        rootView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = rootView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setJavaScriptEnabled(true);
        rootView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        rootView.loadUrl(URL);
        return rootView;
    }

}
