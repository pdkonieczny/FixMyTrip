package com.fixmytrip.train.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.R;
import com.fixmytrip.train.UI.SettingsExpandableListAdapter;
import com.fixmytrip.train.trains.Train;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by philipkonieczny on 12/24/14.
 */
public class SettingsFragment extends Fragment implements View.OnFocusChangeListener {
    public static final String TAG="SettingsFragment";
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE=1;

    private SharedPreferences sharedPref;

    AutoCompleteTextView homeStation;
    AutoCompleteTextView awayStation;

    EditText firstName,lastName,email, phone;
    TrainSystem mTrainSystem;

    public SettingsFragment() {
        // Empty constructor required for fragment subclasses
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        Context context = getActivity();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mTrainSystem = MainActivity.trainSystems.get(getArguments().getInt(Constants.systemID));

        return rootView;

    }

    @Override
    public void onStart()
    {
        super.onStart();

        String[] stations = new String[mTrainSystem.stations.size()];
        int count=0;
        for(TrainStation trainStation : mTrainSystem.stations)
        {
            stations[count++] = trainStation.name;
        }

        homeStation = (AutoCompleteTextView) getActivity().findViewById(R.id.autocomplete_home_station);
        awayStation = (AutoCompleteTextView) getActivity().findViewById(R.id.autocomplete_work_station);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,stations);
        homeStation.setAdapter(adapter);
        awayStation.setAdapter(adapter);
        homeStation.setOnFocusChangeListener(this);

        firstName = (EditText) getActivity().findViewById(R.id.settings_first_name_text);
        lastName = (EditText) getActivity().findViewById(R.id.settings_last_name_text);
        email = (EditText) getActivity().findViewById(R.id.settings_email_text);
        phone = (EditText) getActivity().findViewById(R.id.settings_phone_number_text);

        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE);
        }
        else
        {
            updatePhoneNumberField();
        }

        firstName.setText(sharedPref.getString(getString(R.string.preference_first_name),""));
        lastName.setText(sharedPref.getString(getString(R.string.preference_last_name),""));
        email.setText(sharedPref.getString(getString(R.string.preference_email),""));
        homeStation.setText(sharedPref.getString(getString(R.string.preference_home_station),""));
        awayStation.setText(sharedPref.getString(getString(R.string.preference_away_station),""));

    }

    @Override
    public void onPause()
    {
        super.onPause();

        SharedPreferences.Editor editor = sharedPref.edit();
        if(!firstName.getText().toString().isEmpty())
            editor.putString(getString(R.string.preference_first_name),firstName.getText().toString());

        if(!lastName.getText().toString().isEmpty())
            editor.putString(getString(R.string.preference_last_name),lastName.getText().toString());

        if(!email.getText().toString().isEmpty())
            editor.putString(getString(R.string.preference_email),email.getText().toString());

        TrainStation homeTrainStation = mTrainSystem.getStationByName(homeStation.getText().toString());
        TrainStation awayTrainStation = mTrainSystem.getStationByName(awayStation.getText().toString());

        if(homeTrainStation!=null)
            editor.putString(getString(R.string.preference_home_station),homeTrainStation.name);

        if(awayTrainStation !=null)
            editor.putString(getString(R.string.preference_away_station),awayTrainStation.name);

        editor.commit();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus)
            return;

        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v;
        TrainStation trainStation = mTrainSystem.getStationByName(autoCompleteTextView.getText().toString());

        if(trainStation == null)
        {
            autoCompleteTextView.setText("");
            Toast.makeText(getActivity(), "Invalid Train Station", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    updatePhoneNumberField();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void updatePhoneNumberField()
    {
        TelephonyManager tMgr = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber;
        try {
           phoneNumber = tMgr.getLine1Number();
        }
        catch(Exception e)
        {
            Log.w(TAG,"Unable to get phoneNumber despite having permissions",e);
            return;
        }
        String formattedPhone;
        if(phoneNumber.length()==11)
        {
            formattedPhone = "+" + phoneNumber.charAt(0) + " (" + phoneNumber.substring(1,4) + ") " + phoneNumber.substring(4,7) + " - " + phoneNumber.substring(7);
        }
        else if(phoneNumber.length() == 10)
        {
            formattedPhone = "(" + phoneNumber.substring(0,3) + ") " + phoneNumber.substring(3,6) + " - " + phoneNumber.substring(6);
        }
        else
        {
            formattedPhone = phoneNumber;
        }
        phone.setText(formattedPhone);
        phone.setEnabled(false);
    }
}
