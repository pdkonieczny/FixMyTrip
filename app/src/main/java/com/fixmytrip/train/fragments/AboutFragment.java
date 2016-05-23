package com.fixmytrip.train.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fixmytrip.train.R;
import com.fixmytrip.train.utils.Constants;
import com.fixmytrip.train.utils.Helper;

/**
 * Created by philipkonieczny on 12/24/14.
 */
public class AboutFragment extends Fragment implements View.OnClickListener {
    public static final String TAG="AboutFragment";
    private Button websiteButton;
    private Button googlePlayButton;
    private Button facebookButton;
    private Button twitterButton;
    private Button legalButton;
    private Button dialTriRailButton;

    public AboutFragment() {
        // Empty constructor required for fragment subclasses
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_fragment, container, false);
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Unable to get the application version.");
            e.printStackTrace();
        }
        String version = pInfo.versionName;

        return rootView;

    }

    @Override
    public void onStart()
    {
        super.onStart();

        websiteButton = (Button)getActivity().findViewById(R.id.aboutWebsiteButton);
        websiteButton.setOnClickListener(this);

        googlePlayButton = (Button)getActivity().findViewById(R.id.aboutGooglePlayButton);
        googlePlayButton.setOnClickListener(this);

        facebookButton = (Button)getActivity().findViewById(R.id.aboutFacebookButton);
        facebookButton.setOnClickListener(this);

        twitterButton = (Button)getActivity().findViewById(R.id.aboutTwitterButton);
        twitterButton.setOnClickListener(this);

        legalButton = (Button)getActivity().findViewById(R.id.aboutLegalButton);
        legalButton.setOnClickListener(this);

        dialTriRailButton = (Button)getActivity().findViewById(R.id.aboutDialTriRailButton);
        dialTriRailButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Button clickedButton= (Button) view;

        if(clickedButton.equals(websiteButton))
        {
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.setData(Uri.parse(Constants.FixMyTripURL));
            startActivity(i);
        }
        else if(clickedButton.equals(googlePlayButton))
        {
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.setData(Uri.parse(Constants.GooglePlayURL + getActivity().getPackageName()));
            startActivity(i);
        }
        else if(clickedButton.equals(facebookButton))
        {
            Intent i = getOpenFacebookIntent();
            startActivity(i);
        }
        else if(clickedButton.equals(twitterButton))
        {
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.setData(Uri.parse(Constants.TwitterURL));
            startActivity(i);
        }
        else if(clickedButton.equals(legalButton))
        {
            //TODO: Create Legal Page
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.setData(Uri.parse(Constants.FixMyTripURL));
            startActivity(i);
        }
        else if(clickedButton.equals(dialTriRailButton))
        {
            Helper.callPhoneNumber(getContext());

        }
    }

    public Intent getOpenFacebookIntent() {

        try {
                getActivity().getPackageManager().getPackageInfo(Constants.facebookPackageName, 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FacebookAPIURL));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FacebookURL));
        }
    }
}
