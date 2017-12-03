package ru.kazantsev.testcamera.activity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import net.vrallev.android.cat.Cat;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.testcamera.R;
import ru.kazantsev.testcamera.fragment.LogoFragment;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends BaseActivity implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gravitometer;

    public interface OnTiltDegreesChanged {
        void onTiltDegreesChanged(int degrees, int inclination);
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
        gravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
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
    Lock update = new ReentrantLock();
    Boolean updateGravity = false;
    Boolean updateGeomagnetic = false;
    boolean haveGrav = false;
    float[] inclineG = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(update.tryLock()) {
            try {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_GRAVITY:
                        gravity = event.values.clone();
                        updateGravity = true;
                        haveGrav = true;
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        if (haveGrav) break;    // don't need it, we have better
                        gravity = event.values.clone();
                        updateGravity = true;
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        geomagnetic = event.values.clone();
                        updateGeomagnetic = true;
                        break;
                    default:
                        return;
                }

                if (gravity != null && geomagnetic != null && updateGravity && updateGeomagnetic) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    updateGravity = false;
                    updateGeomagnetic = false;
                    boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
                    if (success) {
                        float orientation[] = new float[9];
                        SensorManager.getOrientation(R, orientation);
                        float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                        float pitch = orientation[1];
                        float roll = orientation[2];

                        inclineG = gravity.clone();
                        double g = Math.sqrt(Math.pow(inclineG[0],  2) + Math.pow(inclineG[1], 2) + Math.pow(inclineG[2], 2));
                        float cosZ = (float) (inclineG[2] / g);
                        int inclination = (int) Math.round(Math.toDegrees(Math.acos(cosZ)));
                        Cat.i("azimu degrees=" + (int) Math.toDegrees(azimut));
                        Cat.i("pitch degrees= " + (int) Math.toDegrees(pitch));
                        Cat.i("roll degrees=" + (int) Math.toDegrees(roll));
                        Cat.i("inclination="+ inclination);
                        if (onTiltDegreesChanged != null) {
                            onTiltDegreesChanged.onTiltDegreesChanged((int) Math.toDegrees(pitch), inclination);
                        }
                    }
                }
            } finally {
                {
                    update.unlock();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
