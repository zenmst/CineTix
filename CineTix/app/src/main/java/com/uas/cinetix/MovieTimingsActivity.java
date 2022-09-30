package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MovieTimingsActivity extends AppCompatActivity {

    Spinner locationSpinner;
    Spinner dateSpinner;
    RadioGroup radioGroup;
    HashMap<String, Integer> locationIdMap;
    HashMap<String, Integer> scheduleIdMap;
    JSONArray schedulesJson;
    ArrayList<String> dates;
    ArrayAdapter<String> dateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_timings);

        locationSpinner = findViewById(R.id.locationSpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        radioGroup = findViewById(R.id.rbtn);
        Button nextBtn = findViewById(R.id.nextBtn);

        Intent intent = getIntent();
        String locationsStr = intent.getStringExtra("locations");
        String schedulesStr = intent.getStringExtra("schedules");

        locationIdMap = new HashMap<>();
        scheduleIdMap = new HashMap<>();

        dates = new ArrayList<>();
        dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dates);

        try {
            JSONArray locationsJson = new JSONArray(locationsStr);
            schedulesJson = new JSONArray(schedulesStr);

            // Set lokasi
            for (int i = 0; i < locationsJson.length(); i++) {
                JSONObject location = locationsJson.getJSONObject(i);
                locationIdMap.put(location.getString("name"), location.getInt("id"));
            }
            List<String> locationArr = new ArrayList<>(locationIdMap.keySet());
            ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationArr);
            locationSpinner.setAdapter(locationAdapter);

            updateTanggal();
            updateWaktu();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTanggal();
                updateWaktu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWaktu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedScheduleId = getSelectedScheduleId();
                Log.i("schedule", "" + selectedScheduleId);
                if (selectedScheduleId == 0) {
                    Toast.makeText(MovieTimingsActivity.this, "Mohon pilih waktu terlebih dahulu", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent i = new Intent(MovieTimingsActivity.this, SeatPlanActivity.class);
                i.putExtra("schedule_id", selectedScheduleId);
                startActivity(i);
            }
        });
    }

    private int getSelectedLocationId() {
        String selectedLocation = locationSpinner.getSelectedItem().toString();
        return locationIdMap.get(selectedLocation);
    }

    private String getSelectedDate() {
        return dateSpinner.getSelectedItem().toString();
    }

    private int getSelectedScheduleId() {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = radioGroup.findViewById(radioButtonId);

        if (radioButton == null) {
            return 0;
        }

        String time = radioButton.getText().toString();
        return scheduleIdMap.get(time);
    }

    private void updateTanggal() {
        try {
            dates.clear();
            for (int i = 0; i < schedulesJson.length(); i++) {
                JSONObject schedule = schedulesJson.getJSONObject(i);
                int locationId = schedule.getInt("location_id");

                if (locationId != getSelectedLocationId()) {
                    continue;
                }

                String date = schedule.getString("date");

                if (!dates.contains(date)) {
                    dates.add(date);
                }
            }

            dateSpinner.setAdapter(dateAdapter);
        } catch (JSONException error) {
            error.printStackTrace();
        }

    }

    private void updateWaktu() {
        try {
            radioGroup.clearCheck();
            radioGroup.removeAllViews();
            scheduleIdMap.clear();

            for (int i = 0; i < schedulesJson.length(); i++) {
                JSONObject schedule = schedulesJson.getJSONObject(i);

                int scheduleId = schedule.getInt("id");
                int locationId = schedule.getInt("location_id");
                String date = schedule.getString("date");
                String time = schedule.getString("time");

                if (locationId != getSelectedLocationId()) {
                    continue;
                }

                if (!date.equals(getSelectedDate())) {
                    continue;
                }

                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(time);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                radioGroup.addView(radioButton, params);

                scheduleIdMap.put(time, scheduleId);
            }
        } catch (JSONException error) {
            error.printStackTrace();
        }

    }
}
