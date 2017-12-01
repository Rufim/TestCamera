package ru.kazantsev.testcamera.activity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import net.vrallev.android.cat.Cat;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.testcamera.R;
import ru.kazantsev.testcamera.fragment.LogoFragment;

public class MainActivity extends BaseActivity implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    public interface OnTiltDegreesChanged {
        void onTiltDegreesChanged(int degrees, boolean lookDown);
    }

    OnTiltDegreesChanged onTiltDegreesChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableNavigationBar = true;
        super.onCreate(savedInstanceState);
        replaceFragment(LogoFragment.class);
        setTitle(getResString(R.string.app_name));
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        initListeners();
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    public void setOnTiltDegreesChanged(OnTiltDegreesChanged onTiltDegreesChanged) {
        this.onTiltDegreesChanged = onTiltDegreesChanged;
    }

    public void initListeners() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onResume() {
        initListeners();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }


    @Override
    protected void handleIntent(Intent intent) {
        
    }

    float[] gravity = null;
    float[] geomagnetic = null;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)  {
            gravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                float pitch = orientation[1];
                float roll = orientation[2];
                Cat.i("azimut degrees=" + Math.toDegrees(azimut));
                Cat.i("azimut pitch=" + Math.toDegrees(pitch));
                Cat.i("azimut roll=" + Math.toDegrees(roll));
                if(onTiltDegreesChanged != null) {
                    onTiltDegreesChanged.onTiltDegreesChanged((int) Math.toDegrees(pitch), roll > 0);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
