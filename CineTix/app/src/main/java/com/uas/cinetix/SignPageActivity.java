package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignPageActivity extends AppCompatActivity {

    private boolean signUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_sign_page);

        final Button SignUp = (Button)findViewById(R.id.SignUp);
        final Button Login = (Button)findViewById(R.id.Login);
        Button Masuk = (Button)findViewById(R.id.Masuk);

        SignUp startFrag = new SignUp ();
        startFrag.setArguments (getIntent ().getExtras ());

        final FragmentManager fragmentManager = getSupportFragmentManager ();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction ();

        fragmentTransaction.replace (R.id.frame, startFrag);
        fragmentTransaction.commit();

        final RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        Masuk.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                EditText edtEmail = findViewById(R.id.edtEmail);
                EditText edtPassword = findViewById(R.id.edtPassword);
                final String email = edtEmail.getText().toString();
                final String password = edtPassword.getText().toString();
                String name = null;

                String url = Config.HOST + "/login-process.php";

                if (signUp) {
                    EditText edtName = findViewById(R.id.edtName);
                    name = edtName.getText().toString();
                    url = Config.HOST + "/register-process.php";
                }

                final String finalName = name;

                final ProgressDialog progressDialog = ProgressDialog.show(SignPageActivity.this, "Loading", "Logging in");

                StringRequest request = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    Log.i("role", jsonResponse.getString("role"));
                                    Intent i;
                                    if (jsonResponse.getString("role").equals("admin")) {
                                        i = new Intent (SignPageActivity.this, AdminPaymentListActivity.class);
                                    } else {
                                        i = new Intent (SignPageActivity.this, HomeActivity.class);
                                    }
                                    startActivity(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        try {
                            String body = new String(error.networkResponse.data);;
                            AlertDialog alertDialog = new AlertDialog.Builder(SignPageActivity.this).create();
                            alertDialog.setTitle("Gagal");
                            alertDialog.setMessage(body);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            progressDialog.dismiss();
                        } catch (Error error1) {
                            error1.printStackTrace();
                        }
                    }
                })
                {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        params.put("password", password);

                        if (signUp) {
                            params.put("name", finalName);
                        }

                        return params;
                    }
                };

                queue.add(request);
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signUp = true;
                SignUp frag1 = new SignUp ();
                frag1.setArguments (getIntent ().getExtras ());

                FragmentManager fragmentManager = getSupportFragmentManager ();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction ();

                fragmentTransaction.add (R.id.frame, frag1);
                fragmentTransaction.replace (R.id.frame, new SignUp ());
                fragmentTransaction.commit();

                SignUp.setBackgroundColor(Color.parseColor("#2699FB"));
                Login.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        });
        Login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signUp = false;
                Login frag2 = new Login ();
                frag2.setArguments (getIntent ().getExtras ());

                FragmentManager fragmentManager = getSupportFragmentManager ();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction ();

                fragmentTransaction.add (R.id.frame, frag2);
                fragmentTransaction.replace (R.id.frame, new Login ());
                fragmentTransaction.commit();

                Login.setBackgroundColor(Color.parseColor("#2699FB"));
                SignUp.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        });
    }
}
