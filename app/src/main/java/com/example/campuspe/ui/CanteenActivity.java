package com.example.campuspe.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campuspe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CanteenActivity extends AppCompatActivity {
    TextView actionName, fare;
    RecyclerView recyclerView;
    MenuAdapter menuAdapter;
    ArrayList<FoodDetails> foodList;
    Button payBtn, applyBtn;
    DatabaseReference databaseReference,databaseReference1;
    FirebaseDatabase database;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen);
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.canteen_action_bar);
        //getSupportActionBar().setElevation(0);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        View view = getSupportActionBar().getCustomView();
        CircleImageView back = view.findViewById(R.id.actionBack);
        actionName = view.findViewById(R.id.actionName);
        databaseReference1 = database.getReference("menu");

        progressBar=findViewById(R.id.progressBar4);
        String canteenName = getIntent().getStringExtra("cName");
        actionName.setText(canteenName);
        payBtn = findViewById(R.id.payBtn);
        applyBtn = findViewById(R.id.applybtn);
        database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = database.getReference("CouponData");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        foodList = new ArrayList<>();
        recyclerView = findViewById(R.id.menuRecycler);

        fare = findViewById(R.id.fare);

progressBar.setVisibility(View.VISIBLE);
        Query mQueryRef = databaseReference1;
        final int[] a = {1};
        mQueryRef.addValueEventListener(new ValueEventListener() {
            private static final String TAG = "";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(TAG, "onDataChange():" + dataSnapshot.toString());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        FoodDetails foodDetails = snapshot.getValue(FoodDetails.class);
                           foodList.add(foodDetails);


                }
                progressBar.setVisibility(View.INVISIBLE);

                menuAdapter = new MenuAdapter(getApplicationContext(),foodList,fare);

                recyclerView.setAdapter(menuAdapter);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.e(TAG, "Failed to read data", error.toException());

            }
        });








        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra("canteen_name", canteenName);
                intent.putExtra("total_fare", Integer.parseInt(fare.getText().toString().substring(3)));
                startActivity(intent);
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        menuAdapter = new MenuAdapter(getApplicationContext(), foodList, fare);
        recyclerView.setAdapter(menuAdapter);
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.coupon_dialog);
        Button verifyBtn = dialog.findViewById(R.id.applyBtn2);
        EditText codeVer = dialog.findViewById(R.id.couponEnter);
        TextView tx = dialog.findViewById(R.id.textView10);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verCode = codeVer.getText().toString();
                String code = verCode;
                Query mQueryRef = databaseReference;

                mQueryRef.addValueEventListener(new ValueEventListener() {
                    private static final String TAG = "";

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "onDataChange():" + dataSnapshot.toString());
                        boolean f=false;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            CouponDetails Data = snapshot.getValue(CouponDetails.class);
                            if(Data.getCode().equals(verCode)){
                                if(Integer.parseInt(fare.getText().toString().substring(3))-Data.getOff()<=0){
                                    Toast.makeText(CanteenActivity.this, "Cannot Apply", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    fare.setText("Rs."+(Integer.parseInt(fare.getText().toString().substring(3))-Data.getOff()));
                                    Toast.makeText(CanteenActivity.this, "Applied!!", Toast.LENGTH_SHORT).show();
                                }
                                f=true;
                                dialog.hide();
                                break;
                            }
                        }
                        if(f==false){
                            Toast.makeText(CanteenActivity.this, "Invalid Coupon Code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.e(TAG, "Failed to read data", error.toException());

                    }
                });
//                fare.setText("Rs."+(Integer.parseInt(fare.getText().toString().substring(3)-));

            }
        });
        dialog.show();
    }
}