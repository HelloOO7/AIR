package cz.hello007.joyslaveapi16;

import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TranscriptorActivity extends AppCompatActivity {

    private Socket s;
    private Socket s2;
    private OutputStream os;
    private OutputStream os2;
    private boolean canDispatch = false;
    private ArrayList<Integer> kcal;
    /*ABXYLRUPDOWNLEFTRIGHTSTARTSELECTCLICKLCLICKR*/
    private boolean[] keyStates = new boolean[14];
    private TranscriptorActivity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_transcriptor);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ip = findViewById(R.id.ipTextBox);
                EditText port = findViewById(R.id.portTextBox);
                confSocket(ip.getText().toString(), Integer.parseInt(port.getText().toString()));
            }
        };
        View.OnClickListener ocl_remap = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RemapActivityIntent = new Intent(mActivity, RemapperActivity.class);
                startActivity(RemapActivityIntent);
            }
        };
        View.OnClickListener ocl_presets = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent PresetsActivityIntent = new Intent(mActivity, PresetsActivity.class);
                startActivity(PresetsActivityIntent);
            }
        };
        Button btn = findViewById(R.id.btnConnect);
        Button btnRemap = findViewById(R.id.btnRemap);
        Button btnPresets = findViewById(R.id.btnPresets);
        btn.setOnClickListener(ocl);
        btnRemap.setOnClickListener(ocl_remap);
        btnPresets.setOnClickListener(ocl_presets);
        ControllerPreset cp = new ControllerPreset(this);
        if (!cp.exists){
            cp = createFallbackPreset();
            cp.savePreset();
        }
        /*TextView tw = findViewById(R.id.curPreset);
        tw.setText(cp.presetName);*/
        kcal = new ArrayList<>();
        for (int i : cp.buttons){
            kcal.add(i);
        }
    }
    private void confSocket(final String ip, final int port){
        Thread t = new Thread(){
            public void run(){
                try {
                    s = new Socket(ip, port);
                    s2 = new Socket(ip, port + 1);
                    os = s.getOutputStream();
                    os2 = s2.getOutputStream();
                    canDispatch = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
    @Override
    public boolean onKeyDown(int keycode, KeyEvent ke){
        return true;
    }
    @Override
    public boolean dispatchKeyEvent(final KeyEvent ke){
        Log.e("test", String.valueOf(ke.getKeyCode()));
        //prepare the bytes to send
        Thread t = new Thread(){
            public void run(){
                if (canDispatch){
                    int isDownByte = (ke.getAction() == KeyEvent.ACTION_DOWN) ? 1 : 0;
                    int kcByte = getNormalizedKeyCode(ke.getKeyCode());
                    Log.e("nkc", String.valueOf(kcByte));
                    if (kcByte != -1) {
                        if ((isDownByte == 1) != keyStates[kcByte]) {
                            try {
                                os.write(isDownByte);
                                os.write(kcByte);
                                os.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            keyStates[kcByte] = isDownByte == 1;
                        }
                    }
                }
            }

        };
        t.start();
        return true;
    }
    @Override
    public boolean dispatchGenericMotionEvent(final MotionEvent me){
        Thread t = new Thread(){
            public void run(){
                if (canDispatch){
                    //assemble axis data
                    try {
                        os2.write(getDpadAxis(me.getAxisValue(MotionEvent.AXIS_HAT_X)));
                        os2.write(getDpadAxis(me.getAxisValue(MotionEvent.AXIS_HAT_Y)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_X)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_Y)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_Z)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_RZ)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_LTRIGGER)));
                        os2.write(float2Byte(me.getAxisValue(MotionEvent.AXIS_RTRIGGER)));
                        //aaand flush
                        os2.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        Log.e("test", String.valueOf(me.getAxisValue(MotionEvent.AXIS_LTRIGGER)));
        t.start();
        return true;
    }
    private static int getDpadAxis(float axisdata){
        return (int)Math.floor(axisdata + 0.5d);
    }
    private static int float2Byte (float value)
    {
        return (int)(value*100);
    }
    private int getNormalizedKeyCode(int keycode){
        for (int i = 0; i < kcal.size(); i++){
            Log.e("kcal", String.valueOf(kcal.get(i)));
        }
        return kcal.indexOf(keycode);
    }
    private ControllerPreset createFallbackPreset(){
        int[] keycodes = new int[]{
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2, 0, 0, 0, 0, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR};
        ControllerPreset test = new ControllerPreset(this, "current", keycodes, new int[8], true);
        Log.e("test2", test.presetName);
        return test;
    }
}
