package com.example.amankumarmishra.qrcode1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {
   private Button scan;
   private FirebaseAuth mAuth;
   Button confirmPayment;
   String value;
    Button gen, submit;
    EditText sapidInputEdiText , scoreInputEditText;
    TextView enterScoreManuallyTextView,orTextView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    User user;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        confirmPayment=findViewById(R.id.confirmPayment);
        sapidInputEdiText = findViewById(R.id.sapidInput);
        sapidInputEdiText.setText("5000");
        scoreInputEditText = findViewById(R.id.scoreInput);
        submit= (Button)findViewById(R.id.submit);
        enterScoreManuallyTextView=findViewById(R.id.enterscoremanually);
        orTextView=findViewById(R.id.ortextview);
        mAuth= FirebaseAuth.getInstance();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           // Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                           // Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void updateUI(FirebaseUser u){
        confirmPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ConfirmActivity.class);
                startActivity(intent);
            }
        });
        //Qr code scanner functionality
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sap = sapidInputEdiText.getText().toString();
                //imp to get thi text inside the onClick listener otherwise it wont take the value that the user enters just before clicking
                //the button.
                //checking if sap correct
                if (sap.equals("5000") || sap.equals("") || sap.length() != 9 || sap.indexOf("5000") != 0) {
                    Toast.makeText(MainActivity.this, "Enter correct sap id", Toast.LENGTH_SHORT).show();
                    return;
                }
                //check if NOT scanned through QR CODE
                if(value==null||value.equals("")){
                    value = scoreInputEditText.getText().toString(); //taking input from edittext
                    //CHECK IF ENTERED VALUE MANUALLY
                    if(value.equals(""))
                    {
                        Toast.makeText(MainActivity.this, "Enter Score First", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                //single event listener added and not the normal one because when "updating" value in db, whenever value gets "updated"
                //onDataChange would be called and itll be an infinite loop.
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //making a path to go to the specific child of db that we need.
                        user = dataSnapshot.child("event_db").child("participants").child(sap).getValue(User.class);
                        if(user==null)
                        {
                            Toast.makeText(MainActivity.this, "User does not exist!!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //user now contains the contents of the pojo file that we made, that we then fetched from the db. so we use
                        //getscore to only get the score.
                        count = user.getScore();
                        //using try in case user enters wrong value
                        try {
                            int score = Integer.parseInt(value);
                            count = count + score;
                            //show Progress bar
                            final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                            progressDialog.setTitle("Updating");
                            progressDialog.show();
                            myRef.child("event_db").child("participants").child(sap).child("score").setValue(count).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Score Updated", Toast.LENGTH_SHORT).show();
                                        //RESET INITIAL STATE OF APP
                                        sapidInputEdiText.setText("5000");
                                        scoreInputEditText.setText("");
                                        scan.setVisibility(View.VISIBLE);
                                        scoreInputEditText.setVisibility(View.VISIBLE);
                                        enterScoreManuallyTextView.setVisibility(View.VISIBLE);
                                        orTextView.setVisibility(View.VISIBLE);
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Please Enter correct Value", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        gen = (Button) findViewById(R.id.gen);
        scan = (Button) findViewById(R.id.scan);
        gen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gIntent = new Intent(MainActivity.this, GeneratorActivity.class);
                startActivity(gIntent);
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                value = result.getContents();
                //HIDE MANUALLY ENTER SCORE FUNCTIONALITY
                scan.setVisibility(View.GONE);
                scoreInputEditText.setVisibility(View.GONE);
                enterScoreManuallyTextView.setVisibility(View.GONE);
                orTextView.setVisibility(View.GONE);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}



//Added progress bar
//User does not exist