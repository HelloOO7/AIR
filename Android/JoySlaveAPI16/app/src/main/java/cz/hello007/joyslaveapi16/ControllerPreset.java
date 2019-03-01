package cz.hello007.joyslaveapi16;

import android.content.Context;
import android.util.Log;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class ControllerPreset {
    public int[] buttons;
    public int[] axes;
    public boolean exists;
    public String presetName;
    private final File f;
    private Ini ini;
    public ControllerPreset(Context context, String name, int[] buttonKCs, int[] axesNums, boolean internal){
        buttons = buttonKCs.clone();
        axes = axesNums.clone();
        presetName = name;
        File path = (internal) ? context.getFilesDir() : context.getExternalFilesDir(null);
        f = new File(path + "/" + name + ".ini");
        ini = new Ini();
        if (f.exists()){
            try {
                ini.load(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 14; i++){
                buttons[i] = ini.get(name, "button" + String.valueOf(i), int.class);
            }
            exists = true;
        }
        else {
            exists = false;
        }
    }

    /**
     * Only use for the default preset in private storage
     * @param context Android app context
     */
    public ControllerPreset(Context context){
        f = new File(context.getFilesDir() + "/current.ini");
        if (f.exists()) {
            String s = "";
            try {
                ini = new Ini(f);
                InputStream is = new FileInputStream(f);
                StringBuilder sb = new StringBuilder();
                is.skip(1);
                int b;
                while (true) {
                    b = is.read();
                    if (b != 93){
                        sb.append((char)b);
                    }
                    else{
                        break;
                    }
                }
                s = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            presetName = s;
            buttons = new int[14];
            for (int i = 0; i < 14; i++) {
                Ini.Section sec = ini.get(presetName);
                buttons[i] = ini.get(presetName,"button" + String.valueOf(i), int.class);
            }
            exists = true;
        }
        else{
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
