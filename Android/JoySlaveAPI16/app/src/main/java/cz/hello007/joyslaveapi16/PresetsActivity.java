package cz.hello007.joyslaveapi16;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class PresetsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ListAdapter la = new PresetsAdapter(this, getPresetNames());
        ListView lw = findViewById(R.id.presetsLW);
        lw.setAdapter(la);
    }

    private ArrayList<String> getPresetNames(){
        File dir = getExternalFilesDir(null);
        String[] flist = new String[0];
        if (dir != null) {
            flist = dir.list();
        }
        return new ArrayList<>(Arrays.asList(flist));
    }
}
