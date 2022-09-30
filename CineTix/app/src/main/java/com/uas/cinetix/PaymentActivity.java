package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private ArrayList<Integer> selectedSeatIdx;
    private ArrayList<String> selectedSeatNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();
        selectedSeatIdx = intent.getIntegerArrayListExtra("seat_index");
        selectedSeatNames = intent.getStringArrayListExtra("seat_names");
        final int scheduleId = intent.getIntExtra("schedule_id", 0);

        TextView seatTxt = findViewById(R.id.seatTxt);
        TextView ticketPriceTxt = findViewById(R.id.ticketPriceTxt);
        TextView totalPriceTxt = findViewById(R.id.totalPriceTxt);
        Button btnPay = findViewById(R.id.btnPay);

        int ticketCount = selectedSeatIdx.size();
        final int totalPrice = ticketCount * 50000;

        seatTxt.setText(TextUtils.join(", ", selectedSeatNames));
        ticketPriceTxt.setText(selectedSeatIdx.size() + " × Rp50.000");
        totalPriceTxt.setText(String.format("Rp%,d", totalPrice).replace(",", "."));

        final RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = ProgressDialog.show(PaymentActivity.this, "Loading", "Memproses pembelian");

                String url = Config.HOST + "/purchase-ticket.php";

                StringRequest request = new StringRequest
                        (Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("response", response);
                                progressDialog.dismiss();

                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    Intent i = new Intent (PaymentActivity.this, UploadPaymentActivity.class);
                                    i.putExtra("ticket_id", jsonResponse.getInt("ticket_id"));
                                    i.putExtra("payment_total", totalPrice);
                                    startActivity(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("seat_index", TextUtils.join(",", selectedSeatIdx));
                        params.put("movie_schedule_id", Integer.toString(scheduleId));

                        return params;
                    }
                };

                queue.add(request);
            }
        });
    }
}
