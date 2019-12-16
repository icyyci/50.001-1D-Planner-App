package com.example.onedee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateAccount extends AppCompatActivity {
    EditText CreateUsername;
    EditText CreatePassword;
    Button buttonCreateAccount;
    TextView SignIn;

    static FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    static DatabaseReference userDatabaseRef = userDatabase.getReference("userDatabase");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        CreateUsername = findViewById(R.id.CreateUsername);
        CreatePassword = findViewById(R.id.CreatePassword);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        SignIn = findViewById(R.id.tvSignIn);

        //Button to bring the user back to the login screen
        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccount.this, LoginScreen.class);
                startActivity(intent);
            }
        });
        /** When the user clicks on create account **/
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tryEmail = CreateUsername.getText().toString();
                final String tryPassword = CreatePassword.getText().toString();
                //To check if the user key in a username and password
                if(tryEmail.equals("") || tryPassword.equals("")){
                    Toast.makeText(CreateAccount.this, "Please enter email/password", Toast.LENGTH_LONG).show();
                }
                else {
                    /** When account is created, the username is saved as a key and the password as the value, also
                     * check if account name is already taken to avoid keys with the same name*/
                    userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //If username already exist in the firebase
                            if(dataSnapshot.hasChild(tryEmail)){
                                Toast.makeText(CreateAccount.this, "Username has been taken", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(CreateAccount.this, "Account Created", Toast.LENGTH_LONG).show();
                                // Add the new user account to firebase
                                DatabaseReference newUserRef= userDatabaseRef.child(tryEmail);                                                      // send to database
                                newUserRef.setValue(tryPassword);
                                // set the password of the new user as the value and the username as the key
                                //Bring the user back to login page
                                Intent intent = new Intent(CreateAccount.this, LoginScreen.class);
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
        });
    }
}
