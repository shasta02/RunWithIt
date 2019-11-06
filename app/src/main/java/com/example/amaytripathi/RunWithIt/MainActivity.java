

package com.example.amaytripathi.RunWithIt;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//Follow comments for code information
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Accessing text views
    TextView tv_steps;
    TextView speed;
    TextView durationTime;

    //Tracks last steps per meter displayed
    int lastSPM = 0;

    //Checks if first time using sensor
    boolean first = true;

    //Tracks upto past 5 steps in a second-Used to calculate SPM
    ArrayList<Integer> stepCounts = new ArrayList<Integer>();

    //Second counter
    int count = 0;
    //Tracks index of ArrayList
    int i = 0;
    //Last ran total steps in a second
    int lastRan = 0;
    //Last total amount of steps displayed
    int lastTotal = 0;
    //Initial value of Pedometer to calculate
    float init;
    //Current beat of song
    int songSPM = 150;

    //Used for checking for no sensor update
    int lastSteps = 0;
    int repeats = 0;

    //Sensor for pedometer object
    SensorManager sensorManager;

    //Checks if phone is running
    boolean running = false;
    //Used to run timer thread
    boolean runThread = true;

    //MediaPlayer Object
    MediaPlayer mediaPlayer;

    //Buttons from display
    Button startButton;
    Button stopButton;

    //Checks whether play or stop should be shown
    boolean playShown = true;

    /**
     * Creation method
     * Initializes TextView, SensorManager, Buttons, MediaPlayer Objects
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Building Sensor
        tv_steps = (TextView) findViewById(R.id.tv_steps);
        tv_steps.setText("---");
        speed = (TextView) findViewById(R.id.speed);
        durationTime = (TextView) findViewById(R.id.durationTime);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //MediaPlayer
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.imaginedragons);

        //Button
        startButton = findViewById(R.id.button);
        stopButton = findViewById(R.id.stopButton);
        stopButton.setVisibility(View.INVISIBLE);

        startButton.setOnClickListener(onClickListener);
        stopButton.setOnClickListener(onClickListener);


        //end oncreate here
    }

    /**
     * Checks if button has been clicked
     * playShown is used to check if stop or start has been press
     * Adjusts visibility, starts/terminates program
     */
    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (playShown){
                startButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                playShown = false;
                startProgram();
            }
            else{
                stopButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                playShown = true;
                terminate();
                speed.setText("000");
                tv_steps.setText("X");
                durationTime.setText("0:00");
            }
        }
    };

    /**
     * Terminates timer thread
     * Resets variables needed to run
     */
    public void terminate(){
        //Terminating thread
        runThread = false;

        //Resetting variables for run
        mediaPlayer.stop();
        stepCounts.clear();
        i = 0;
        lastRan = 0;
        lastTotal = 0;
        songSPM = 150;
        first = true;
        lastSPM = 0;
        count = -1;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hello);
    }

    /**
     * Runs program
     * Contains thread timer that calculates SPM, duration, adjusts song
     */
    protected void startProgram() {

        mediaPlayer.start();
        runThread = true;

        //Timer
        Thread t = new Thread(){
            @Override
            public void run(){
                while (runThread){
                    try{
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int lastSPMUse = lastSPM;
                                if (count % 20 == 0){
                                    if (lastSPMUse <= 150 && lastSPMUse < songSPM){
                                        mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                                R.raw.imaginedragons);
                                        songSPM = 150;
                                        mediaPlayer.start();
                                    }
                                    else if (lastSPMUse <= 160 && lastSPMUse > songSPM){
                                        mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                                R.raw.wewillrockyou);
                                        songSPM = 160;
                                        mediaPlayer.start();
                                    }
                                    else if (lastSPMUse <= 170 && lastSPMUse > songSPM){
                                        mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                                R.raw.stressedout);
                                        songSPM = 170;
                                        mediaPlayer.start();
                                    }
                                    else if (lastSPMUse > 170 && lastSPMUse > songSPM){
                                        mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                                R.raw.keepyourheadup);
                                        songSPM = 180;
                                        mediaPlayer.start();
                                    }
                                }
                                count++;
                                lastSPM = findSpeed();
                                String zero = "";
                                if (count % 60 < 10){
                                    zero = "0";
                                }
                                durationTime.setText(String.valueOf(count / 60) + ":" + zero +
                                                     String.valueOf(count % 60));
                                if (lastSPM == lastSteps){
                                    repeats++;
                                }
                                else{
                                    repeats = 0;
                                }
                                lastSteps = lastSPM;
                                if (repeats >= 4){
                                    speed.setText("000");
                                }
                                else{
                                    speed.setText(String.valueOf(lastSPM));
                                }
                            }
                        });
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    if (!runThread){
                        break;
                    }
                }
            }

        };

        t.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;

        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else{
            Toast.makeText(this, "Phone does not support accelerometer!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;

        mediaPlayer.stop();
        mediaPlayer.release();

        //sensorManager.unregisterListener(this);
    }


    public int findSpeed(){
        if (i < 5){
            stepCounts.add(lastRan);
        }
        else{
            stepCounts.remove(0);
            stepCounts.add(lastRan);
        }
        i++;

        double totalOfStepCounts = 0;
        //int countLows = 0;
        for (int j = 0; j < stepCounts.size(); j++){
            //int initStepCount = stepCounts.get(0);
            totalOfStepCounts += stepCounts.get(j);
            /*
            if ((initStepCount == 1 || initStepCount == 2 || initStepCount == 3) &&
                    stepCounts.get(j) == initStepCount){
                countLows++;
            }
            */
        }

        int spm = (int)(totalOfStepCounts *= 12);

        /*
        if (countLows >= 5){
            spm = 0;
        }
        */

        return(spm);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running){
            if (first){
                init = event.values[0];
                first = false;
            }

            int total = (int)(event.values[0] - init);
            lastRan = total - lastTotal;
            lastTotal = total;
            if (lastTotal == 0){
                tv_steps.setText("X");
            }
            else{
                tv_steps.setText(String.valueOf(lastTotal));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



