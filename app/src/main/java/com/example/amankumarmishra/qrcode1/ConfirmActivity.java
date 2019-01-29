package com.example.amankumarmishra.qrcode1;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfirmActivity extends AppCompatActivity {
EditText sapid,password,participantsapid;
Spinner eventid;
Button login,update;
LinearLayout update_layout,login_layout;
String[] eventids={"Choose Event Id","bigheadcoder","buyout","brogrammer","csgo","debattle","fifa","firewall","pitchup","pubg","showbiz"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        sapid=findViewById(R.id.sapid);
        password=findViewById(R.id.password);
        participantsapid=findViewById(R.id.participantsapid);
        eventid=findViewById(R.id.eventid);
        login=findViewById(R.id.login);
        update=findViewById(R.id.update);
        login_layout=findViewById(R.id.login_layout);
        update_layout=findViewById(R.id.update_layout);
        update_layout.setVisibility(View.GONE);
        final ProgressDialog progressDialog=new ProgressDialog(ConfirmActivity.this);
        progressDialog.setTitle("Processing");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,eventids);
        eventid.setAdapter(adapter);
        eventid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0)
                participantsapid.setText(eventids[position]);
                else {
                    participantsapid.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sap=sapid.getText().toString().trim();
                final String pass=password.getText().toString().trim();
                if(sap.equals("")||pass.equals("")) {
                    Toast.makeText(ConfirmActivity.this, "Enter sapid password", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                FirebaseDatabase.getInstance().getReference().child("PaytmUsers").child(sap).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.getValue().equals(pass))
                                {
                                    login_layout.setVisibility(View.GONE);
                                    update_layout.setVisibility(View.VISIBLE);
                                    progressDialog.dismiss();
                                }
                                else {
                                    Toast.makeText(ConfirmActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(ConfirmActivity.this, "User does not exists !", Toast.LENGTH_SHORT).show();
                            }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ConfirmActivity.this, "Check you internet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String participantsap=participantsapid.getText().toString().trim();
                if (eventid.getSelectedItemPosition()==0)
                {
                    Toast.makeText(ConfirmActivity.this, "Select Event ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(participantsap.equals(""))
                {
                    Toast.makeText(ConfirmActivity.this, "Enter Team ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String eventId=eventid.getSelectedItem().toString();
                progressDialog.show();
                FirebaseDatabase.getInstance().getReference().child("event_db").child("events").child(eventId).child("teams").child(participantsap).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            FirebaseDatabase.getInstance().getReference().child("event_db").child("events").child(eventId).child("teams").child(participantsap).child("confirmed").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(ConfirmActivity.this, "Value Updated", Toast.LENGTH_SHORT).show();
                                        participantsapid.setText("");
                                        eventid.setSelection(0);
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(ConfirmActivity.this, "Wrong Team Id", Toast.LENGTH_SHORT).show();
                            participantsapid.setText("");
                            progressDialog.dismiss();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        });
    }
}
