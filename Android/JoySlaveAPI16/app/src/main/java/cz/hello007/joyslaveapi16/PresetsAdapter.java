package cz.hello007.joyslaveapi16;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

class PresetsAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<String> list;
    private Context con;

    public PresetsAdapter(Context context, ArrayList<String> list){
        con = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_preset, null);
        }

        TextView ptext = view.findViewById(R.id.textMain);
        ptext.setText(list.get(position).substring(0, list.get(position).length() - 4));

        Button btnRemove = view.findViewById(R.id.btnRemove);
        Button btnApply = view.findViewById(R.id.btnApply);

        btnRemove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(con);
                b.setTitle("Are you sure?");
                b.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removePreset(list.get(position));
                        list.remove(position);
                        dialog.dismiss();
                        notifyDataSetChanged();
                    }
                });
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                b.show();
            }
        });
        btnApply.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
            }
        });

        return view;
    }
    private void removePreset(String name){
        File f = new File(con.getExternalFilesDir(null) + "/" + name);
        Log.i("Files/Remove: ", f.getAbsolutePath());
        f.delete();
    }
}
