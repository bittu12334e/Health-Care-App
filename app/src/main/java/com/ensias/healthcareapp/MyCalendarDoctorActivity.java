package com.ensias.healthcareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.Interface.ITimeSlotLoadListener;
import com.ensias.healthcareapp.adapter.MyTimeSlotAdapter;
import com.ensias.healthcareapp.databinding.ActivityMyCalendarDoctorBinding;
import com.ensias.healthcareapp.model.TimeSlot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.hedyhidoury.calendar.horizontallibrary.HorizontalCalendarView;
import com.hedyhidoury.calendar.horizontallibrary.listener.OnDatePickedListener;

import dmax.dialog.SpotsDialog;

public class MyCalendarDoctorActivity extends AppCompatActivity implements ITimeSlotLoadListener {

    private ActivityMyCalendarDoctorBinding binding;
    private DocumentReference doctorDoc;
    private ITimeSlotLoadListener iTimeSlotLoadListener;
    private AlertDialog alertDialog;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCalendarDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        iTimeSlotLoadListener = this;
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(this)
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));

        binding.recycleTimeSlot2.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        binding.recycleTimeSlot2.setLayoutManager(gridLayoutManager);

        // Get the calendar view reference
        HorizontalCalendarView calendarView = binding.calendarView2;

        // Set date picked listener
        calendarView.setDatePickedListener(new OnDatePickedListener() {
            @Override
            public void OnDatePicked(Date date) {
                Calendar pickedDate = Calendar.getInstance();
                pickedDate.setTime(date);

                if (Common.currentDate.getTimeInMillis() != pickedDate.getTimeInMillis()) {
                    Common.currentDate = pickedDate;
                    loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(pickedDate.getTime()));
                }
            }
        });
    }

    private void loadAvailabelTimeSlotOfDoctor(String currentDoctor, String bookDate) {
        alertDialog.show();

        doctorDoc = FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(Common.CurreentDoctor);

        doctorDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("Doctor")
                                .document(Common.CurreentDoctor)
                                .collection(bookDate);

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) {
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    } else {
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult())
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        binding.recycleTimeSlot2.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        binding.recycleTimeSlot2.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}