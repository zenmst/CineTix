package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.murgupluoglu.seatview.Seat;
import com.murgupluoglu.seatview.SeatView;
import com.murgupluoglu.seatview.SeatViewListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SeatPlanActivity extends AppCompatActivity {

    TextView selectedSeatsTxt;
    TextView seatCountTxt;
    TextView totalPriceTxt;
    ArrayList<Integer> selectedSeatIdx;
    ArrayList<String> selectedSeatNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_plan);

        Intent intent = getIntent();
        final int scheduleId = intent.getIntExtra("schedule_id", 0);

        final View mainView = findViewById(R.id.mainView);
        final SeatView seatView = findViewById(R.id.seatView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        selectedSeatsTxt = findViewById(R.id.selectedSeatsTxt);
        seatCountTxt = findViewById(R.id.seatCountTxt);
        totalPriceTxt = findViewById(R.id.totalPriceTxt);
        Button btnNext = findViewById(R.id.btnNext);

        selectedSeatIdx = new ArrayList<>();
        selectedSeatNames = new ArrayList<>();

        mainView.setVisibility(View.INVISIBLE);

        final RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        String url = Config.HOST + "/seats.php?schedule_id=" + scheduleId;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray seatsJson = response.getJSONArray("seats");

                            ArrayList<Integer> takenSeats = new ArrayList<>();

                            for (int i = 0; i < seatsJson.length(); i++) {
                                takenSeats.add(seatsJson.getInt(i));
                            }

                            int rowCount = 9;
                            int colCount = 9;
                            HashMap<String, String> rowNames = new HashMap<>();

                            seatView.initSeatView(createSeatsArray(takenSeats, rowCount, colCount, rowNames), rowCount, colCount, rowNames);
                            mainView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException error) {
                            error.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        queue.add(request);

        seatView.setSeatClickListener(new SeatViewListener() {
            @Override
            public void seatReleased(Seat seat, HashMap<String, Seat> hashMap) {
                updateSeats(hashMap);
            }

            @Override
            public void seatSelected(Seat seat, HashMap<String, Seat> hashMap) {
                updateSeats(hashMap);
            }

            @Override
            public boolean canSelectSeat(Seat seat, HashMap<String, Seat> hashMap) {
                return seat.getType() == Seat.TYPE.INSTANCE.getSELECTABLE();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSeatIdx.isEmpty()) {
                    return;
                }

                Intent i = new Intent(SeatPlanActivity.this, PaymentActivity.class);
                i.putIntegerArrayListExtra("seat_index", selectedSeatIdx);
                i.putStringArrayListExtra("seat_names", selectedSeatNames);
                i.putExtra("schedule_id", scheduleId);
                startActivity(i);
            }
        });
    }

    private Seat[][] createSeatsArray(ArrayList<Integer> takenSeats, int rowCount, int colCount, HashMap<String, String> rowNames) {
        Seat[][] seatArray = new Seat[rowCount][colCount];
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        for (int i = 0; i < rowCount; i++) {
            String rowName = Character.toString(alphabet[i]);
            rowNames.put(Integer.toString(i), rowName);
            for (int j = 0; j < colCount; j++) {
                int seatIndex = (i * colCount) + j;
                Seat seat = new Seat();
                seat.setId(Integer.toString(seatIndex));
                seat.setSeatName(rowName + (j + 1));
                seat.setRowIndex(i);
                seat.setColumnIndex(j);
                seat.setRowName(rowName);
                seat.setSelected(false);
                seat.setDrawableResourceName("seat_available");
                seat.setSelectedDrawableResourceName("seat_selected");

                if (takenSeats.contains(seatIndex)) {
                    seat.setType(Seat.TYPE.INSTANCE.getUNSELECTABLE());
                    seat.setDrawableResourceName("seat_notavailable");
                } else {
                    seat.setType(Seat.TYPE.INSTANCE.getSELECTABLE());
                }
                seatArray[i][j] = seat;
            }
        }

        return seatArray;
    }

    private void updateSeats(HashMap<String, Seat> hashMap) {
        selectedSeatIdx.clear();
        selectedSeatNames.clear();
        ArrayList<String> selectedSeat = new ArrayList<>();

        for (Seat seat1 : hashMap.values()) {
            selectedSeat.add(seat1.getSeatName());
            selectedSeatIdx.add(Integer.parseInt(seat1.getId()));
            selectedSeatNames.add(seat1.getSeatName());
        }
        selectedSeatsTxt.setText(TextUtils.join(", ", selectedSeat));

        int seatCount = selectedSeat.size();
        int totalPrice = seatCount * 50000;

        seatCountTxt.setText(Integer.toString(seatCount));
        totalPriceTxt.setText(String.format("Rp%,d", totalPrice).replace(",", "."));
    }
}
