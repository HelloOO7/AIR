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

public class TranscriptorActivity extends AppCompatActivity {

    private Socket s;
    private Socket s2;
    private OutputStream os;
    private OutputStream os2;
    private boolean canDispatch = false;
    /*ABXYLRUPDOWNLEFTRIGHTSTARTSELECTCLICKLCLICKR*/
    private boolean[] keyStates = new boolean[14];
    private TranscriptorActivity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        InputManager im = (InputManager) this.getSystemService(Context.INPUT_SERVICE);
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
        switch (keycode){
            case KeyEvent.KEYCODE_BUTTON_A:
                return 0;
            case KeyEvent.KEYCODE_BUTTON_B:
                return 1;
            case KeyEvent.KEYCODE_BUTTON_X:
                return 2;
            case KeyEvent.KEYCODE_BUTTON_Y:
                return 3;
            case KeyEvent.KEYCODE_BUTTON_L1:
                return 4;
            case KeyEvent.KEYCODE_BUTTON_R1:
                return 5;
            case KeyEvent.KEYCODE_BUTTON_START:
                return 10;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return 11;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                return 12;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                return 13;
            /*case KeyEvent.KEYCODE_DPAD_UP:
                return 6;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return 7;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return 8;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return 9;*/ //deprecate the d-pad as its implementation is broken in Android and is used for both the joystick and pad
        }
        return -1;
    }
}
