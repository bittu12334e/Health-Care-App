package com.ensias.healthcareapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.databinding.FragmentBookingStep3Binding;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BookingStep3Fragment extends Fragment {

    private FragmentBookingStep3Binding binding;
    private SimpleDateFormat simpleDateFormat;
    private LocalBroadcastManager localBroadcastManager;

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG", "onReceive: heave been receiver");
            setData();
        }
    };

    private void setData() {
        binding.txtBookingBerberText.setText(Common.CurrentDoctorName);
        binding.txtBookingTimeText.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append("at")
                .append(simpleDateFormat.format(Common.currentDate.getTime())));
        binding.txtBookingPhone.setText(Common.CurrentPhone);
        binding.txtBookingType.setText(Common.Currentaappointementatype);
    }

    public BookingStep3Fragment() {
        // Required empty public constructor
    }

    public static BookingStep3Fragment newInstance(String param1, String param2) {
        return new BookingStep3Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());

        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
        binding = null; // Avoid memory leaks
    }

    private static BookingStep3Fragment instance;
    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentBookingStep3Binding.inflate(inflater, container, false);

        // Set up click listener for confirm button
        binding.btnConfirm.setOnClickListener(v -> confirmeApointement());

        return binding.getRoot();
    }

    private void confirmeApointement() {
        ApointementInformation apointementInformation = new ApointementInformation();
        apointementInformation.setApointementType(Common.Currentaappointementatype);
        apointementInformation.setDoctorId(Common.CurreentDoctor);
        apointementInformation.setDoctorName(Common.CurrentDoctorName);
        apointementInformation.setPatientName(Common.CurrentUserName);
        apointementInformation.setPatientId(Common.CurrentUserid);
        apointementInformation.setChemin("Doctor/"+Common.CurreentDoctor+"/"+Common.simpleFormat.format(Common.currentDate.getTime())+"/"+String.valueOf(Common.currentTimeSlot));
        apointementInformation.setType("Checked");
        apointementInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append("at")
                .append(simpleDateFormat.format(Common.currentDate.getTime())).toString());
        apointementInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(Common.CurreentDoctor)
                .collection(Common.simpleFormat.format(Common.currentDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        bookingDate.set(apointementInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getActivity().finish();
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                        Common.currentTimeSlot = -1;
                        Common.currentDate = Calendar.getInstance();
                        Common.step = 0;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseFirestore.getInstance().collection("Doctor").document(Common.CurreentDoctor)
                                .collection("apointementrequest").document(apointementInformation.getTime().replace("/","_")).set(apointementInformation);
                        FirebaseFirestore.getInstance().collection("Patient").document(apointementInformation.getPatientId()).collection("calendar")
                                .document(apointementInformation.getTime().replace("/","_")).set(apointementInformation);
                    }
                });
    }
}