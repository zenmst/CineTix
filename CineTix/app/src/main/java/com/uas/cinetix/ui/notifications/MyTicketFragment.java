package com.uas.cinetix.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.uas.cinetix.Config;
import com.uas.cinetix.PaymentActivity;
import com.uas.cinetix.R;
import com.uas.cinetix.UploadPaymentActivity;
import com.uas.cinetix.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Ticket {
    int id;
    String title;
    String location;
    String date;
    String time;
    ArrayList<String> seats;
    boolean isPaid;
    boolean isConfirmed;
    String code;
    int paymentTotal;
}

public class MyTicketFragment extends Fragment {

    ArrayList<Ticket> ticketArrayList;

    Context context;
    ImageLoader imageLoader;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_ticket, container, false);

        context = container.getContext();

        final ListView lv = root.findViewById(R.id.ticketList);
        final ProgressBar progressBar = root.findViewById(R.id.progressBar);

        final RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        ticketArrayList = new ArrayList<>();

        String url = Config.HOST + "/ticket-list.php";

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray ticketsJson = response.getJSONArray("tickets");

                            for (int i = 0; i < ticketsJson.length(); i++) {
                                JSONObject ticketJson = ticketsJson.getJSONObject(i);
                                Ticket ticket = new Ticket();
                                ticket.id = ticketJson.getInt("id");
                                ticket.title = ticketJson.getString("title");
                                ticket.location = ticketJson.getString("location");
                                ticket.date = ticketJson.getString("date");
                                ticket.time = ticketJson.getString("time");

                                JSONArray seatJson = ticketJson.getJSONArray("seats");
                                ticket.seats = new ArrayList<>();
                                for (int j = 0; j < seatJson.length(); j++) {
                                    ticket.seats.add(seatJson.getString(j));
                                }

                                ticket.isPaid = !ticketJson.isNull("payment_created");
                                ticket.isConfirmed = !ticketJson.isNull("payment_confirmed");
                                ticket.code = ticketJson.getString("code");
                                ticket.paymentTotal = Integer.parseInt(ticketJson.getString("payment_total"));

                                ticketArrayList.add(ticket);
                            }

                            progressBar.setVisibility(View.GONE);

                            lv.setAdapter(new MyAdapter());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        queue.add(request);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ticket ticket = ticketArrayList.get(position);

                if (!ticket.isPaid && !ticket.isConfirmed) {
                    Intent i = new Intent (context, UploadPaymentActivity.class);
                    i.putExtra("ticket_id", ticket.id);
                    i.putExtra("payment_total", ticket.paymentTotal);
                    startActivity(i);
                }
            }
        });

        return root;
    }

    private class MyAdapter extends ArrayAdapter<Ticket> {
        MyAdapter() {
            super(context, R.layout.ticket_row, ticketArrayList);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = inflater.inflate(R.layout.ticket_row, null);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Ticket ticket = ticketArrayList.get(position);

            holder.movieTitle.setText(ticket.title);
            holder.ticketLocation.setText(ticket.location);
            holder.ticketDateTime.setText(ticket.date + " - " + ticket.time);
            holder.ticketSeats.setText(TextUtils.join(", ", ticket.seats));
            holder.ticketPrice.setText(String.format("Rp%,d", ticket.paymentTotal).replace(",", "."));

            if (ticket.isConfirmed) {
                holder.ticketPaymentStatus.setText("Kode Booking: " + ticket.code);
                holder.ticketPaymentStatus.setTypeface(null, Typeface.BOLD);
            } else if (ticket.isPaid) {
                holder.ticketPaymentStatus.setText("Menunggu konfirmasi pembayaran");
            } else {
                holder.ticketPaymentStatus.setText("Belum dibayar");
            }

            row.setBackgroundResource(R.drawable.list_design);

            return row;
        }
    }

    private class ViewHolder {
        TextView movieTitle;
        TextView ticketLocation;
        TextView ticketDateTime;
        TextView ticketSeats;
        TextView ticketPrice;
        TextView ticketPaymentStatus;

        ViewHolder(View v) {
            movieTitle = v.findViewById(R.id.movieTitle);
            ticketLocation = v.findViewById(R.id.ticketLocation);
            ticketDateTime = v.findViewById(R.id.ticketDateTime);
            ticketSeats = v.findViewById(R.id.ticketSeats);
            ticketPrice = v.findViewById(R.id.ticketPrice);
            ticketPaymentStatus = v.findViewById(R.id.ticketPaymentStatus);
        }
    }
}
