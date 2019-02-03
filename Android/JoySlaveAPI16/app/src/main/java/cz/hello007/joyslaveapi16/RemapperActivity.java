package cz.hello007.joyslaveapi16;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ini4j.Ini;

public class RemapperActivity extends AppCompatActivity {

    private TextView maptext;
    private TextView kctext;
    private String[] buttonNames = {"A", "B", "X", "Y", "Shoulder L", "Shoulder R", "D-Pad UP", "D-Pad DOWN", "D-Pad LEFT", "D-Pad RIGHT", "Start", "Select", "Click L", "Click R"};
    private int[] keycodes = new int[14];
    private int index = 0;
    private int lastAction = KeyEvent.ACTION_UP;
    private RemapperActivity mActivity;
    private Ini ini;
    private boolean exists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_remapper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        maptext = findViewById(R.id.btnTextView);
        kctext = findViewById(R.id.textBtnKC);
        update();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.homeAsUp){
            this.finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && lastAction == KeyEvent.ACTION_UP){
            advance(event.getKeyCode());
        }
        lastAction = event.getAction();
        return true;
    }
    public void advance(int keycode){
        keycodes[index] = keycode;
        if (index != 13){
            index ++;
            while (buttonNames[index].startsWith("D-Pad")){
                index ++;
            }
        }
        else {
            Toast.makeText(this, "All set! You can now further review your bindings or save them as a preset.", Toast.LENGTH_LONG).show();
        }
        update();
    }
    public void update(){
        maptext.setText(buttonNames[index]);
        kctext.setText((keycodes[index] == 0) ? "No button assigned" : ("Bound to keycode " + keycodes[index]));
    }
    public void revert(View view){
        if (index != 0){
            index --;
            while (buttonNames[index].startsWith("D-Pad")){
                index -= 1;
            }
        }
        update();
    }
    public void advanceNull(View view){
        if (index != 13) {
            index +=1;
            update();
        }
    }
    private String name;
    public void makePreset(View view){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        b.setTitle("Name your preset");
        b.setCancelable(true);
        b.setView(et);
        DialogInterface.OnClickListener negocl = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };
        DialogInterface.OnClickListener posocl = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = et.getText().toString();
                Log.e("test", name);
                ControllerPreset cp = new ControllerPreset(mActivity, name, keycodes, new int[8]);
                cp.savePreset();
                Toast.makeText(mActivity, "Saved!", Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        };
        b.setNegativeButton("Cancel", negocl);
        b.setPositiveButton("Confirm", posocl);
        b.show();
    }
}
