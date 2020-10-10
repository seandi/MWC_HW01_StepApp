package com.example.stepapp.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.stepapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class HomeFragment extends Fragment {
    MaterialButtonToggleGroup materialButtonToggleGroup;
    MaterialButtonToggleGroup buttonToggleGroupStepMode;
    MaterialButton stepDetectorButton;

    // Text view and Progress Bar variables
    public TextView stepsCountTextView;
    public ProgressBar stepsCountProgressBar;

    private StepCounterListener listener;

    // TODO 1: ACC sensors.
    private Sensor accelerometerSensor;
    private SensorManager sensorManager;


    // Step Detector sensor
    private Sensor stepDetectorSensor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // TODO 9: Initialize the TextView variable
        stepsCountTextView = (TextView) root.findViewById(R.id.stepsCount);
        // TODO HW: Initialize the ProgressBar variable
        stepsCountProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        stepsCountProgressBar.setMax(100);

        // TODO 2: Get an instance of the sensor manager.
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // instance of the sensor manager for the step detector
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


        // TODO 11
        // instantiate the StepCounterListener
        listener = new StepCounterListener(stepsCountTextView, stepsCountProgressBar, getContext());

        // Toggle group button
        materialButtonToggleGroup = (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (group.getCheckedButtonId() == R.id.toggleStart && isChecked) {

                    //Place code related to Start button
                    Toast.makeText(getContext(), "START", Toast.LENGTH_SHORT).show();


                    // TODO 3: Check if the Accelerometer sensor exists
                    if(accelerometerSensor != null){
                        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    }else{
                        Toast.makeText(getContext(),R.string.acc_not_available,Toast.LENGTH_SHORT).show();
                    }


                    // TODO HW: Check if the Step detector sensor exists
                    if(stepDetectorSensor != null){
                        sensorManager.registerListener(listener, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    }else{
                        Toast.makeText(getContext(), R.string.step_not_available, Toast.LENGTH_SHORT).show();
                    }


                }
                else if (group.getCheckedButtonId() == R.id.toggleStop && isChecked) {
                    //Place code related to Stop button
                    Toast.makeText(getContext(), "STOP", Toast.LENGTH_SHORT).show();

                    // TODO 4: Unregister the listener
                    sensorManager.unregisterListener(listener);
                }
                else if(group.getCheckedButtonId() == R.id.toggleReset && isChecked){
                    Toast.makeText(getContext(), "RESET", Toast.LENGTH_SHORT).show();
                    listener = new StepCounterListener(stepsCountTextView, stepsCountProgressBar, getContext());

                }


            }
        });

        buttonToggleGroupStepMode= (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroupStepMode);
        buttonToggleGroupStepMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if(group.getCheckedButtonId() == R.id.toggleAcc && isChecked){
                    listener.enable_accelerator();
                    Toast.makeText(getContext(), "Switched to accelerometer", Toast.LENGTH_SHORT).show();
                }
                else if(group.getCheckedButtonId() == R.id.toggleAndroidStepDetector && isChecked){
                    listener.enable_step_counter();
                    Toast.makeText(getContext(), "Switched to Android step detector", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // enable Android step detector
        root.findViewById(R.id.toggleAndroidStepDetector).performClick();
        //////////////////////////////////////
        return root;



    }
}

// Sensor event listener
class StepCounterListener implements SensorEventListener {

    private long lastUpdate = 0;

    // ACC Step counter
    public static int mACCStepCounter = 0;
    ArrayList<Integer> mACCSeries = new ArrayList<Integer>();
    private double accMag = 0;
    private int lastXPoint = 1;
    int stepThreshold = 6;

    // Android step detector
    private int mAndroidStepCount = 0;

    // TextView
    private TextView stepsCountTextView;
    // ProgressBar
    private ProgressBar stepsCountProgressBar;
    private Context context;
    boolean use_acc = false;
    boolean use_step_counter = true;


    //TODO 10
    public StepCounterListener(TextView stepsCountTextView, ProgressBar stepsCountProgressBar, Context context){
        this.stepsCountTextView = stepsCountTextView;
        this.stepsCountProgressBar = stepsCountProgressBar;
        this.context = context;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            // TODO 5: Get the sensor type
            case Sensor.TYPE_LINEAR_ACCELERATION:

            // TODO 6: Get sensor's values
            float x = event.values[0];
            float y = event.values[1];
            float z =event.values[2];

            //////////////////////////// -- PRINT ACC VALUES -- ////////////////////////////////////
            // TODO 7: Uncomment the following code
                // Timestamp
                long timeInMillis = System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

                // Convert the timestamp to date
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String date = jdf.format(timeInMillis);

                // print a value every 1000 ms
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > 1000) {
                    lastUpdate = curTime;

                    Log.d("ACC", "X: " + String.valueOf(x) + " Y: " + String.valueOf(y) + " Z: "
                            + String.valueOf(z) + " t: " + String.valueOf(date));

                }

            ////////////////////////////////////////////////////////////////////////////////////////

            // TODO 8: Compute the ACC magnitude
            accMag = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
            //Update the Magnitude series
            mACCSeries.add((int) accMag);

            /// STEP COUNTER ACC ////
            // Calculate ACC peaks and steps

            peakDetection();

            break;

            // case Step detector
            case Sensor.TYPE_STEP_DETECTOR:
                // Calculate the number of steps
                if(use_step_counter){
                    countSteps(event.values[0]);
                    stepsCountTextView.setText(String.valueOf(mAndroidStepCount));
                    stepsCountProgressBar.setProgress(mAndroidStepCount);
                }

        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void peakDetection() {
        int windowSize = 20;

        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
         */
        int highestValX = mACCSeries.size(); // get the length of the series
        if (highestValX - lastXPoint < windowSize) { // if the segment is smaller than the processing window skip it
            return;
        }

        List<Integer> valuesInWindow = mACCSeries.subList(lastXPoint,highestValX);

        lastXPoint = highestValX;

        int forwardSlope = 0;
        int downwardSlope = 0;

        List<Integer> dataPointList = new ArrayList<Integer>();

        for (int p =0; p < valuesInWindow.size(); p++){
            dataPointList.add(valuesInWindow.get(p));
        }


        for (int i = 0; i < dataPointList.size(); i++) {
            if (i == 0) {
            }
            else if (i < dataPointList.size() - 1) {
                forwardSlope = dataPointList.get(i + 1) - dataPointList.get(i);
                downwardSlope = dataPointList.get(i)- dataPointList.get(i - 1);

                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i) > stepThreshold ) {
                    mACCStepCounter += 1;
                    Log.d("ACC STEPS: ", String.valueOf(mACCStepCounter));

                    //TODO 12: update the text view
                    //use only step counter to update step count on screen
                    if(use_acc) {
                        stepsCountTextView.setText(String.valueOf(mACCStepCounter));
                        if (mACCStepCounter == 100) {
                            Toast.makeText(context, "GOAL REACHED!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    // Calculate the number of steps from the step detector
    private void countSteps(float step) {
        mAndroidStepCount += 1;
        Log.d("STEP DETECTOR STEPS: ", String.valueOf(mAndroidStepCount));
        if(mAndroidStepCount == 100){
            Toast.makeText(context, "GOAL REACHED!", Toast.LENGTH_SHORT).show();
        }
    }

    public void enable_accelerator(){
        use_acc = true;
        use_step_counter = false;
    }

    public void enable_step_counter(){
        use_acc = false;
        use_step_counter = true;
    }
}

