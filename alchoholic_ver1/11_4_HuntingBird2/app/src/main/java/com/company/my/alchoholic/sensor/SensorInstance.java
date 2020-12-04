package com.company.my.alchoholic.sensor;

import android.widget.Button;

import java.util.LinkedList;
import java.util.List;

public class SensorInstance implements Sensor{

    private SensorStatus status;
    private int[] fds = new int[8];
    private InputThread inputThread;
    private List<ButtonCallback>[] btnCallbacks;
    private List<ButtonCallback>[] switchCallbacks;

    static {
        System.loadLibrary("sensor");
    }
    private native int loadSensors(int[] arr);
    private native int unloadSensors(int[] arr);
    private native int dotmSpin(int dotmFd);
    private native int motorSpin(int motorFd, int speed, int second);

    private void init(){

        status = SensorStatus.READY;
        btnCallbacks = new LinkedList[Sensor.NUM_BTN];
        for (int i = 0; i < btnCallbacks.length; i++){
            btnCallbacks[i] = new LinkedList<>();
        }

        switchCallbacks = new LinkedList[Sensor.NUM_SWITCH];
        for(int i = 0; i < switchCallbacks.length; i++){
            switchCallbacks[i] = new LinkedList<>();
        }


    }

    @Override
    public void readySensor() {
        loadSensors(fds);
        boolean flag = true;
        for (int idx = 0; idx < fds.length; idx++) {
            System.out.println("idx " + idx + " : " + fds[idx]);
            if (fds[idx] == -1) {
                flag = false;
            }
        }
        if (flag) status = SensorStatus.START;
        else status = SensorStatus.FAIL;


        inputThread = new InputThread(fds.clone(), btnCallbacks, switchCallbacks);
        inputThread.start();
        System.out.println("Run Thread");
    }

    @Override
    public void stopSensor() {
        unloadSensors(fds);
    }

    @Override
    public SensorStatus getSensorStatus(){
        return status;
    }

    @Override
    public void runMotor(int direction, int speed) {}

    @Override
    public void runMotor(int direction, int speed, int time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("MOTOR On");
                motorSpin(fds[SensorType.STEP_MOTOR.getSensorCode()], 10, 5);
            }
        }).start();
    }

    @Override
    public void stopMotor() {}

    @Override
    public void startAnimatedDot(int code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("DOTM Animation On");
                dotmSpin(fds[SensorType.DOT_MATRIX.getSensorCode()]);
            }
        }).start();
    }

    @Override
    public void stopAnimatedDot() {}

    @Override
    public void clearDot() {}

    @Override
    public void registerButtonCallback(int btnNum, ButtonCallback callback) {
        btnCallbacks[btnNum].add(callback);
    }

    @Override
    public void unregisterButtonCallback(int btnNum, ButtonCallback callback) {
        btnCallbacks[btnNum].remove(callback);
    }

    @Override
    public void registerSwitchCallback(int switchNum, ButtonCallback callback) {
        switchCallbacks[switchNum].add(callback);
    }

    @Override
    public void unregisterSwitchCallback(int switchNum, ButtonCallback callback) {
        switchCallbacks[switchNum].remove(callback);
    }


    //
    // Singleton pattern
    //
    private SensorInstance() { init(); };
    private static SensorInstance instance;
    public static SensorInstance getInstance(){
        if (instance == null){
            instance = new SensorInstance();
        }
        return instance;
    }

}
