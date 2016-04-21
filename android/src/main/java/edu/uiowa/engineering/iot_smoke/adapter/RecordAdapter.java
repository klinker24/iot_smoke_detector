package edu.uiowa.engineering.iot_smoke.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.uiowa.engineering.iot_smoke.airQuality.model.AirQualityRecord;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<AirQualityRecord> records;
    private Activity activity;

    public RecordAdapter(List<AirQualityRecord> records, Activity activity) {
        this.records = records;
        this.activity = activity;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecordViewHolder(activity.getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_1, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        holder.text.setText(records.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {

        public TextView text;

        public RecordViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }

    }
}
