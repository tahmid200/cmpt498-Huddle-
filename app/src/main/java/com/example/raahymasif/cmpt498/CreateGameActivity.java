package com.example.raahymasif.cmpt498;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.example.raahymasif.cmpt498.Model.CreatePosts;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.example.raahymasif.cmpt498.Model.CreatePosts;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.places.Place;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;




public class CreateGameActivity extends Activity {
    MaterialEditText LocationText, NumberOfPlayersText, InfoText, DateText;
    Button PostButton, CancelButton;
    String uniqueId = new String();
    Place eventAddress;
    String usersJoined = " ";
    AutoCompleteTextView SportText;

    //on date set is used for getting the date
    DatePickerDialog.OnDateSetListener mDatesetListener;

    //get the username;
    //Bundle extras = getIntent().getExtras();
    String postedBy;// = extras.getString("username");
    String email;// = extras.getString("email");

    //autocomplete text for the sports that can be chosen
    private static final String[] sportCategory = new String[]{
            "Baseball", "Basketball", "Bowling","Cricket","Curling","Esports","Football","Hockey","Lacrosse","Rugby","Soccer"
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_creategame);

        //LocationText = (MaterialEditText)findViewById(R.id.LocationText);
        final PlaceAutocompleteFragment LocationText = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.LocationText);
        LocationText.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        ((EditText) LocationText.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint(" Click To Select A Location ");
        ((EditText) LocationText.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(20);

        LocationText.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                eventAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR", status.getStatusMessage());
            }
        });


        SportText = (AutoCompleteTextView) findViewById(R.id.SportText);
        //SportText = (MaterialEditText)findViewById(R.id.SportText);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,sportCategory);
        SportText.setAdapter(adapter);


        //getting the date
        DateText = (MaterialEditText) findViewById(R.id.DateText);

        DateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int date = cal.get(Calendar.DAY_OF_MONTH);
            }
        });


        NumberOfPlayersText = (MaterialEditText)findViewById(R.id.NumberOfPlayerText);
        InfoText  = (MaterialEditText)findViewById(R.id.InfoText);

        //buttons for posting and cancelling
        PostButton = (Button)findViewById(R.id.PostButton);
        CancelButton = (Button)findViewById(R.id.CancelButton);

        // Initialize the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_post = database.getReference("Posts");

        //get the username;
        Bundle extras = getIntent().getExtras();
        postedBy = extras.getString("username");
        email = extras.getString("email");

        //when cancel is clicked
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homePage = new Intent(CreateGameActivity.this,HomePageActivity.class);
                homePage.putExtra("user_name", postedBy);
                homePage.putExtra("email",email);
                startActivity(homePage);
            }
        });
        //when post is clicked
        PostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(CreateGameActivity.this);
                mDialog.setMessage("Please wait...");
                mDialog.show();

                //generates the ID
                uniqueId = generateUniqueId();

                //get the username;
                /*Bundle extras = getIntent().getExtras();
                postedBy = extras.getString("username");
                email = extras.getString("email");*/

                table_post.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if uniqueID already exists
                        if(dataSnapshot.child(uniqueId.toString()).exists()) {
                            mDialog.dismiss();
                            //if unique id exists it will redo the unique id to find the one that doesn't exist
                            while(dataSnapshot.child(uniqueId.toString()).exists()){
                                uniqueId = generateUniqueId();
                            }
                        }

                        else
                        {
                            mDialog.dismiss();
                            // add to the database
                            //(info, address, num of players, sport, user, joined users)
                            CreatePosts createPosts = new CreatePosts(InfoText.getText().toString(),eventAddress.getAddress().toString(),NumberOfPlayersText.getText().toString(), SportText.getText().toString(), postedBy.toString(), usersJoined.toString(), uniqueId.toString());
                            table_post.child(uniqueId.toString()).setValue(createPosts);
                            //Toast.makeText(CreateGameActivity.this, "Post Submitted!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(CreateGameActivity.this, uniqueId, Toast.LENGTH_SHORT).show();
                            finish();

                            Intent homePage = new Intent(CreateGameActivity.this,HomePageActivity.class);
                            homePage.putExtra("user_name", postedBy);
                            homePage.putExtra("email", email);
                            startActivity(homePage);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //no code
                    }
                });

            }
        });


    }
    //generates unique Id for the posts to distinguish between them
    //later have to implement characters to make it more unique
    public String generateUniqueId(){
        String string = new String();
        Random rand = new Random();
        int num = rand.nextInt(1000);
        string = Integer.toString(num);


        return string;
    }


}
