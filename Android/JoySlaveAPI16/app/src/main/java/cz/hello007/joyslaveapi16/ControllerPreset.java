package cz.hello007.joyslaveapi16;

import android.content.Context;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

class ControllerPreset {
    public int[] buttons;
    public int[] axes;
    private boolean exists;
    public String presetName;
    private File f;
    private Wini ini;
    public ControllerPreset(Context context, String name, int[] buttonKCs, int[] axesNums){
        buttons = buttonKCs.clone();
        axes = axesNums.clone();
        presetName = name;
        f = new File(context.getExternalFilesDir(null) + "/" + name + ".ini");
        ini = new Wini();
        if (f.exists()){
            for (int i = 0; i < 14; i++){
                buttons[i] = ini.get(name, "button" + String.valueOf(i), int.class);
            }
            exists = true;
        }
        else {
            exists = false;
        }
    }
    public void savePreset(){
        if (!exists){
            for (int i = 0; i < 14; i++){
                ini.add(presetName, "button" + String.valueOf(i), this.buttons[i]);
            }
            try {
                ini.store(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
