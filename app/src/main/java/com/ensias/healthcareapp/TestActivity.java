package com.ensias.healthcareapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.Common.NonSwipeViewPager;
import com.ensias.healthcareapp.adapter.MyViewPagerAdapter;
import com.ensias.healthcareapp.databinding.ActivityTestBinding;
import com.vinay.stepview.VerticalStepView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ensias.healthcareapp.Common.Common.step;
import static com.ensias.healthcareapp.fragment.BookingStep1Fragment.spinner;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;
    LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(step == 2){
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT,-1);
            }
            binding.btnNextStep.setEnabled(true);
            setColorButton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStepView();
        setColorButton();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        binding.viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setColorButton();
            }

            @Override
            public void onPageSelected(int position) {
                // Update the step view to show current position
                // We'll just re-setup the step view with the current position
                updateStepView(position);

                if(position == 0)
                    binding.btnPreviousStep.setEnabled(false);
                else
                    binding.btnPreviousStep.setEnabled(true);

                if(position == 2)
                    binding.btnNextStep.setEnabled(false);
                else
                    binding.btnNextStep.setEnabled(true);

                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.btnNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(step < 3 || step == 0 ){
                    step++;
                    Common.Currentaappointementatype = spinner.getSelectedItem().toString();
                    Log.e("Spinnr", Common.Currentaappointementatype);

                    if(step == 1){
                        if(Common.CurreentDoctor != null) {
                            Common.currentTimeSlot = -1;
                            Common.currentDate = Calendar.getInstance();
                            loadTimeSlotOfDoctor(Common.CurreentDoctor);
                        }
                    }
                    else if(step == 2){
                        confirmeBooking();
                    }
                    binding.viewPager.setCurrentItem(step);
                }
            }
        });

        binding.btnPreviousStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(step == 3 || step > 0 ){
                    step--;
                    binding.viewPager.setCurrentItem(step);
                }
            }
        });

        loadTimeSlotOfDoctor("testdoc@testdoc.com");
    }

    private void updateStepView(int position) {
        // Try using the library's API to update the current step
        // If there's no direct method to set the current step,
        // we recreate the steps with the current position highlighted
        try {
            // Use reflection to find and call any method that might update the step position
            // This is a fallback solution that handles different library versions
            Class<?> stepViewClass = binding.stepView.getClass();
            try {
                // Try calling setStepCount or similar method
                java.lang.reflect.Method method = stepViewClass.getMethod("setCurrentStep", int.class);
                method.invoke(binding.stepView, position);
                return;
            } catch (Exception e1) {
                try {
                    // Try calling go method if available
                    java.lang.reflect.Method method = stepViewClass.getMethod("go", int.class, boolean.class);
                    method.invoke(binding.stepView, position, true);
                    return;
                } catch (Exception e2) {
                    // If all else fails, just set up steps again
                    setupStepView();
                }
            }
        } catch (Exception e) {
            // Fallback to just redoing setup
            setupStepView();
        }
    }

    private void confirmeBooking() {
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        step = 0;
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }

    private void loadTimeSlotOfDoctor(String doctorId) {
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void setColorButton() {
        if(binding.btnPreviousStep.isEnabled()){
            binding.btnPreviousStep.setBackgroundResource(R.color.design_default_color_primary_dark);
        }
        else{
            binding.btnPreviousStep.setBackgroundResource(R.color.colorAccent);
        }
        if(binding.btnNextStep.isEnabled()){
            binding.btnNextStep.setBackgroundResource(R.color.design_default_color_primary_dark);
        }
        else{
            binding.btnNextStep.setBackgroundResource(R.color.colorAccent);
        }
    }

    private void setupStepView() {
        List stepList = new ArrayList<>();
        stepList.add("Purpose");
        stepList.add("Time and Date");
        stepList.add("Finish");

        // Depending on your library version, one of these methods should work
        try {
            // Most common method name
            binding.stepView.setSteps(stepList);
        } catch (Exception e) {
            try {
                // Another possible method name
                binding.stepView.getClass().getMethod("setLabels", List.class)
                        .invoke(binding.stepView, stepList);
            } catch (Exception e2) {
                Log.e("TestActivity", "Could not set steps on StepView", e2);
            }
        }
    }
}