package com.fixmytrip.train.notifications;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fixmytrip.train.R;

/**
 * Created by philipkonieczny on 11/1/15.
 */
public class UpdateAdapter extends ArrayAdapter<TrainStatus> {

    Context context;
    int layoutResourceId;
    TrainStatus data[] = null;

    public UpdateAdapter(Context context, int layoutResourceId, TrainStatus[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        UpdateHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new UpdateHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.update_list_Icon);
            holder.txtTitle = (TextView)row.findViewById(R.id.update_list_Title);
            holder.txtBody = (TextView)row.findViewById(R.id.update_list_Body);

            row.setTag(holder);
        }
        else
        {
            holder = (UpdateHolder)row.getTag();
        }

        TrainStatus status = data[position];
        holder.txtTitle.setText(Parser.getNotificationTitle(status));
        holder.imgIcon.setImageResource(Parser.getUpdateIcon(context, status));
        holder.txtBody.setText(Parser.getNotificationBigText(status));

        return row;
    }

    static class UpdateHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtBody;
    }

}
