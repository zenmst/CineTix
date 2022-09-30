package com.uas.cinetix;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminPaymentListActivity extends ListActivity {

    private ArrayList<Payment> values;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment_list);

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        values = new ArrayList<>();

        final RequestQueue queue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();
        imageLoader = VolleySingleton.getInstance(getApplicationContext()).getImageLoader();

        String url = Config.HOST + "/admin";

        final JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray paymentsJson = response.getJSONArray("payments");

                            for (int i = 0; i < paymentsJson.length(); i++) {
                                JSONObject paymentJson = paymentsJson.getJSONObject(i);
                                Payment payment = new Payment();
                                payment.id = paymentJson.getInt("ticketId");
                                payment.waktuPembelian = paymentJson.getString("ticketCreatedAt");
                                payment.waktuPembayaran = paymentJson.getString("paymentCreatedAt");
                                payment.bukti = Config.HOST + "/uploads/payment_proofs/" + paymentJson.getString("proof");
                                payment.namaUser = paymentJson.getString("userName");
                                payment.pembayaran = paymentJson.getInt("payment_total");
                                payment.namaBank = paymentJson.getString("bank_name");
                                payment.namaRek = paymentJson.getString("account_name");
                                payment.noRek = paymentJson.getString("account_number");

                                if (paymentJson.isNull("paymentCreatedAt")) {
                                    payment.status = "Belum dibayar";
                                } else if (paymentJson.isNull("confirmed_at")) {
                                    payment.status = "Menunggu konfirmasi";
                                } else {
                                    payment.status = "Lunas";
                                }

                                values.add(payment);
                            }

                            progressBar.setVisibility(View.GONE);

                            ArrayAdapter<Payment> adapter = new ArrayAdapter(AdminPaymentListActivity.this,
                                    android.R.layout.simple_list_item_1, values);

                            setListAdapter(adapter);
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

        ListView listView = findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                final Payment payment = values.get(position);

                final Dialog dialog = new Dialog(AdminPaymentListActivity.this);
                dialog.setContentView(R.layout.payment_dialog);
                dialog.setTitle("Pilih Aksi");
                dialog.show();

                NetworkImageView proofImg = dialog.findViewById(R.id.paymentProof);

                if (payment.status.equalsIgnoreCase("Menunggu konfirmasi")) {
                    TextView txtBankName = dialog.findViewById(R.id.txtBankName);
                    TextView txtAccountName = dialog.findViewById(R.id.txtAccountName);
                    TextView txtAccountNum = dialog.findViewById(R.id.txtAccountNum);

                    txtBankName.setText("\t\t\t\t\t\t\t\tBank: " + payment.namaBank);
                    txtAccountName.setText("\t\t\tAtas Nama: " + payment.namaRek);
                    txtAccountNum.setText("\tNo. Rekening: " + payment.noRek);

                    proofImg.setImageUrl(payment.bukti, imageLoader);
                } else {
                    proofImg.setVisibility(View.GONE);
                }

                Button btnDelete = dialog.findViewById(R.id.btnDeletePayment);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder konfirm = new AlertDialog.Builder(AdminPaymentListActivity.this);
                        konfirm.setTitle("Hapus Pemesanan Tiket");
                        konfirm.setMessage("Anda yakin akan menghapus pemesanan tiket ini?");
                        konfirm.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ProgressDialog.show(AdminPaymentListActivity.this, "Loading", "Menghapus...");

                                String url = Config.HOST + "/admin/payment-delete.php?id=" + payment.id;
                                StringRequest request1 = new StringRequest(Request.Method.GET, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                progressDialog.dismiss();
                                                finish();
                                                startActivity(getIntent());
                                                Toast.makeText(AdminPaymentListActivity.this, "Pemesanan tiket berhasil dihapus", Toast.LENGTH_LONG).show();
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });

                                queue.add(request1);
                            }
                        });
                        konfirm.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        konfirm.show();
                        dialog.dismiss();
                    }
                });

                Button btnConfirm = dialog.findViewById(R.id.btnConfirmPayment);

                if (!payment.status.equalsIgnoreCase("Menunggu konfirmasi")) {
                    btnConfirm.setVisibility(View.INVISIBLE);
                }

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder konfirm = new AlertDialog.Builder(AdminPaymentListActivity.this);
                        konfirm.setTitle("Konfirmasi Pemesanan Tiket");
                        konfirm.setMessage("Anda yakin akan mengkonfirmasi pembayaran tiket ini?");
                        konfirm.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ProgressDialog.show(AdminPaymentListActivity.this, "Loading", "Mengkonfirmasi...");

                                String url = Config.HOST + "/admin/payment-accept.php?id=" + payment.id;
                                StringRequest request1 = new StringRequest(Request.Method.GET, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                progressDialog.dismiss();
                                                finish();
                                                startActivity(getIntent());
                                                Toast.makeText(AdminPaymentListActivity.this, "Pemesanan tiket berhasil diterima", Toast.LENGTH_LONG).show();
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });

                                queue.add(request1);
                            }
                        });
                        konfirm.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        konfirm.show();
                        dialog.dismiss();
                    }
                });
            }
        });
    }
}

class Payment {
    int id;
    String waktuPembelian;
    String waktuPembayaran;
    String namaUser;
    int pembayaran;
    String status;
    String bukti;
    String namaBank;
    String namaRek;
    String noRek;

    @NonNull
    @Override
    public String toString() {
        return "\n Waktu Pembelian: " + waktuPembelian +
                "\n\t\t\t\t\t\t Nama User: " + namaUser +
                "\n\t\t\t\t\t\t\t\t\t\tJumlah: " + pembayaran +
                "\n\t\t\t\t\t\t\t\t\t\t\tStatus: " + status +
                "\n";
    }
}