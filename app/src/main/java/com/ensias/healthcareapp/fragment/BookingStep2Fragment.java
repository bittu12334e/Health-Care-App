package com.ensias.healthcareapp.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.Interface.ITimeSlotLoadListener;
import com.ensias.healthcareapp.adapter.MyTimeSlotAdapter;
import com.ensias.healthcareapp.databinding.FragmentBookingStepTwoBinding;
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

public class BookingStep2Fragment extends Fragment implements ITimeSlotLoadListener {

    private FragmentBookingStepTwoBinding binding;
    private DocumentReference doctorDoc;
    private ITimeSlotLoadListener iTimeSlotLoadListener;
    private AlertDialog dialog;
    private LocalBroadcastManager localBroadcastManager;
    private SimpleDateFormat simpleDateFormat;

    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0);
            loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));
        }
    };

    private void loadAvailabelTimeSlotOfDoctor(String doctorId, final String bookDate) {
        dialog.show();

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

    static BookingStep2Fragment instance;

    public static BookingStep2Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep2Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotLoadListener = this;

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
        binding = null; // Clean up the binding
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentBookingStepTwoBinding.inflate(inflater, container, false);
        View itemView = binding.getRoot();

        init(itemView);
        loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(Common.currentDate.getTime()));

        return itemView;
    }

    private void init(View itemView) {
        binding.recycleTimeSlot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        binding.recycleTimeSlot.setLayoutManager(gridLayoutManager);

        // Get the calendar view safely
        HorizontalCalendarView calendarView = (HorizontalCalendarView) itemView.findViewById(binding.calendarView.getId());

        // Set listener properly
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




    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        binding.recycleTimeSlot.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        binding.recycleTimeSlot.setAdapter(adapter);
        dialog.dismiss();
    }
}