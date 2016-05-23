package com.fixmytrip.train.UI;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.fixmytrip.train.R;

import java.util.List;
import java.util.Map;

/**
 * Created by philipkonieczny on 9/5/15.
 */
public class SettingsExpandableListAdapter extends BaseExpandableListAdapter {
    private Activity context;
    private List<String> stations;
    private String header;

    public SettingsExpandableListAdapter(Activity context, String header,
                                         List<String> trains)
    {
        this.context = context;
        this.header = header;
        this.stations = trains;
    }
    public void setGroupHeader(String str)
    {
        header=str;
    }
    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return stations.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return header;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return stations.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.settings_list_header,
                    null);
        }
        TextView headerText = (TextView) convertView.findViewById(R.id.settingListHeader);
        headerText.setTypeface(null, Typeface.BOLD);
        headerText.setText(headerName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String station = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.settings_list_item, null);
        }

        TextView stationName = (TextView) convertView.findViewById(R.id.settingTrainItemText);

        stationName.setText(station);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
