package com.example.autoalert.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.autoalert.R;

import java.util.List;
import java.util.Map;

public class SensorAdapter extends ArrayAdapter<Sensor> {

    private Map<String, String> sensorValues;

    public SensorAdapter(Context context, List<Sensor> sensors, Map<String, String> sensorValues) {
        super(context, 0, sensors);
        this.sensorValues = sensorValues;
    }

    public void updateSensorNames(List<Sensor> sensors) {
        clear();
        addAll(sensors);
        notifyDataSetChanged();
    }


    public void updateSensorValues(Map<String, String> sensorValues) {
        this.sensorValues = sensorValues;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Sensor sensor = getItem(position);
        String sensorName = getSensorName(sensor.getType());
        String sensorValue = sensorValues.get(sensorName);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sensor_list_item, parent, false);
        }

        TextView sensorText = convertView.findViewById(R.id.sensor_text);
        sensorText.setText(String.format("%s\n%s", sensorName, sensorValue));
        sensorText.setTextColor(Color.WHITE);

        return convertView;
    }

    private String getSensorName(int type) {
        switch (type) {
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            default:
                return "Unknown Sensor";
        }
    }
}
