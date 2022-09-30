package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadPaymentActivity extends AppCompatActivity {

    EditText edtBankName;
    EditText edtPemilikRek;
    EditText edtNomorRek;
    int ticketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_payment);

        Button btnChoose = findViewById(R.id.btnChoose);
        edtBankName = findViewById(R.id.edtBankName);
        edtPemilikRek = findViewById(R.id.edtPemilikRek);
        edtNomorRek = findViewById(R.id.edtNomorRek);
        TextView txtJumlah = findViewById(R.id.txtJumlah);

        Intent intent = getIntent();
        ticketId = intent.getIntExtra("ticket_id", 0);
        int paymentTotal = intent.getIntExtra("payment_total", 0);

        txtJumlah.setText(String.format("Rp%,d", paymentTotal).replace(",", "."));

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent image = new Intent(Intent.ACTION_GET_CONTENT);
                image.setType("image/jpeg");
                startActivityForResult(Intent.createChooser(image, "Pilih bukti pembayaran"), 0);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final RequestQueue queue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

        if (requestCode == 0 && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap =
                        MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] imageInByte = baos.toByteArray();

                final ProgressDialog progressDialog = ProgressDialog.show(UploadPaymentActivity.this, "Loading", "Mengunggah bukti pembayaran");

                // Upload gambar
                String url = Config.HOST + "/payment-process.php";
                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        progressDialog.dismiss();
                        Intent i = new Intent(UploadPaymentActivity.this, HomeActivity.class);
                        i.putExtra("jumpTo", "tickets");
                        finishAffinity();
                        startActivity(i);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            progressDialog.dismiss();
                            if (error.networkResponse.statusCode == 400) {
                                AlertDialog alertDialog = new AlertDialog.Builder(UploadPaymentActivity.this).create();
                                alertDialog.setTitle("Gagal");
                                alertDialog.setMessage("Mohon lengkapi semua input");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        } catch (Error error1) {
                            error1.printStackTrace();
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("ticket_id", Integer.toString(ticketId));
                        params.put("account_name", edtPemilikRek.getText().toString());
                        params.put("bank_name", edtBankName.getText().toString());
                        params.put("account_number", edtNomorRek.getText().toString());
                        return params;
                    }

                    @Override
                    protected Map<String, DataPart> getByteData() throws AuthFailureError {
                        Map<String, DataPart> params = new HashMap<>();
                        params.put("payment_proof", new DataPart("proof.jpg", imageInByte, "image/jpeg"));
                        return params;
                    }
                };

                queue.add(multipartRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
