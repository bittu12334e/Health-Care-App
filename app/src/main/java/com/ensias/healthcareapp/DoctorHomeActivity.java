package com.ensias.healthcareapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.databinding.ActivityDoctorHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class DoctorHomeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    static String doc;
    private ActivityDoctorHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and get instance of binding
        binding = ActivityDoctorHomeBinding.inflate(getLayoutInflater());
        // Set the content view to the root view of the binding
        setContentView(binding.getRoot());

        // Initialize common variables
        Common.CurreentDoctor = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Common.CurrentUserType = "doctor";

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Profile button
        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, ProfileDoctorActivity.class);
            startActivity(intent);
        });

        // My Calendar button
        binding.myCalendarBtn.setOnClickListener(v -> {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DoctorHomeActivity.this, MyCalendarDoctorActivity.class);
            startActivity(intent);
        });

        // Sign Out button
        binding.signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Request button
        binding.btnRequst.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, ConfirmedAppointmensActivity.class);
            startActivity(intent);
        });

        // List Patients button
        binding.listPatients.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, MyPatientsActivity.class);
            startActivity(intent);
        });

        // Appointment button
        binding.appointement.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorHomeActivity.this, DoctorAppointementActivity.class);
            startActivity(intent);
        });
    }

    public void showDatePickerDialog(Context wf) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                wf,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "month_day_year: " + month + "_" + dayOfMonth + "_" + year;
        openPage(view.getContext(), doc, date);
    }

    private void openPage(Context wf, String d, String day) {
        Intent i = new Intent(wf, AppointementActivity.class);
        i.putExtra("key1", d + "");
        i.putExtra("key2", day);
        i.putExtra("key3", "doctor");
        wf.startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up binding when activity is destroyed
        binding = null;
    }
}