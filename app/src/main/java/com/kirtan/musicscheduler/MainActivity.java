package com.kirtan.musicscheduler;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected SharedPreferences myPrefs;
    protected SharedPreferences.Editor editor;
    protected HashMap<String, ArrayList<String>> hashMap;
    protected ArrayList<String> timeList;
    protected final String SPLITTER = "/////",
            MONDAY = "Monday",
            TUESDAY = "Tuesday",
            WEDNESDAY = "Wednesday",
            THURSDAY = "Thursday",
            FRIDAY = "Friday",
            SATURDAY = "Saturday",
            SUNDAY = "Sunday";
    protected ExpandableListView listView;
    protected ExpandableListAdapter adapter;
    protected AlarmManager alarmManager;
    private ArrayList<PendingIntent> pendingIntent;
    private static MainActivity inst;
    ArrayList<MediaPlayer> mps;
    public View row;
    protected Button sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    protected int toggled;
    protected String day;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ExpandableListView) findViewById(R.id.listView);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mps = new ArrayList<>();
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        sunday = (Button) findViewById(R.id.Sunday);
        monday = (Button) findViewById(R.id.Monday);
        tuesday = (Button) findViewById(R.id.Tuesday);
        wednesday = (Button) findViewById(R.id.Wednesday);
        thursday = (Button) findViewById(R.id.Thursday);
        friday = (Button) findViewById(R.id.Friday);
        saturday = (Button) findViewById(R.id.Saturday);

        pendingIntent = new ArrayList<>();

        updateList();

        final Calendar calendar = Calendar.getInstance();
        toggle(calendar.get(Calendar.DAY_OF_WEEK));
        toggled = calendar.get(Calendar.DAY_OF_WEEK);

        sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = SUNDAY;
                toggled = 1;
                toggleButton(sunday);

            }
        });
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = MONDAY;
                toggled = 2;
                toggleButton(monday);
            }
        });
        tuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = TUESDAY;
                toggled = 3;
                toggleButton(tuesday);
            }
        });
        wednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = WEDNESDAY;
                toggled = 4;
                toggleButton(wednesday);
            }
        });
        thursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = THURSDAY;
                toggled = 5;
                toggleButton(thursday);
            }
        });
        friday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = FRIDAY;
                toggled = 6;
                toggleButton(friday);

            }
        });
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = SATURDAY;
                toggled = 7;
                toggleButton(saturday);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, 0);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    showDelete(groupPosition, childPosition);


                    return true;
                }
                else if(ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Delete this schedule");
                    builder.setMessage("WARNING: This will delete the schedule!");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int gp = ExpandableListView.getPackedPositionGroup(id);
                            String s = myPrefs.getString(day, "");
                            s = s.replace(timeList.get(gp)+SPLITTER, "");
                            editor.putString(day, s);
                            for(String x: myPrefs.getString(day + timeList.get(gp),"").split(SPLITTER)){
                                editor.remove(x);
                            }
                            editor.remove(day + timeList.get(gp)).apply();
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.DAY_OF_WEEK, toggled);
                            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeList.get(gp).substring(0,2)));
                            calendar.set(Calendar.MINUTE, Integer.parseInt(timeList.get(gp).substring(3)));
                            calendar.set(Calendar.SECOND, 0);
                            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                            PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, (int)calendar.getTimeInMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmManager.cancel(pi);
                            updateList();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }

                return true;
            }
        });
    }

    private void toggle(int i) {
        switch (i){
            case 1: day = SUNDAY;
                toggleButton(sunday);
                break;
            case 2: day = MONDAY;
                toggleButton(monday);
                break;
            case 3: day = TUESDAY;
                toggleButton(tuesday);
                break;
            case 4: day = WEDNESDAY;
                toggleButton(wednesday);
                break;
            case 5: day = THURSDAY;
                toggleButton(thursday);
                break;
            case 6: day = FRIDAY;
                toggleButton(friday);
                break;
            case 7: day = SATURDAY;
                toggleButton(saturday);
                break;


            default: break;
        }
    }

    private void toggleButton(Button day) {
        setBackColor();
        setTextColor();
        day.setBackgroundColor(Color.GREEN);
        day.setTextColor(Color.WHITE);
        updateList();
    }

    private void setTextColor() {
        sunday.setTextColor(Color.BLACK);
        monday.setTextColor(Color.BLACK);
        tuesday.setTextColor(Color.BLACK);
        wednesday.setTextColor(Color.BLACK);
        thursday.setTextColor(Color.BLACK);
        friday.setTextColor(Color.BLACK);
        saturday.setTextColor(Color.BLACK);
    }

    private void setBackColor() {
        sunday.setBackgroundColor(Color.WHITE);
        monday.setBackgroundColor(Color.WHITE);
        tuesday.setBackgroundColor(Color.WHITE);
        wednesday.setBackgroundColor(Color.WHITE);
        thursday.setBackgroundColor(Color.WHITE);
        friday.setBackgroundColor(Color.WHITE);
        saturday.setBackgroundColor(Color.WHITE);
    }

    private void showDelete(final int g,final int c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    delete(g, c);
                }
            }
        });
        builder.show();
    }

    private void delete(int g, int c) {
        String s = timeList.get(g);
        String tmp = "";
        ArrayList<String> al = hashMap.get(s);
        al.remove(c);
        hashMap.put(s, al);
        for(String x: al){
            tmp += x + SPLITTER;
        }
        editor.putString(day + s, tmp).apply();
        updateList();
    }

    private void updateList() {
        timeList = new ArrayList<>();
        hashMap = new HashMap<>();
        for (String s : myPrefs.getString(day, "").split(SPLITTER)) {
            if (!s.trim().equals("")) {
                timeList.add(s);
            }
        }
        Collections.sort((List) timeList);
        for (String s : timeList) {
            ArrayList<String> al = new ArrayList<>();
            for (String x : myPrefs.getString(day + s, "").split(SPLITTER)) {
                if(!x.trim().equals(""))
                {
                    al.add(x);
                }
            }
            hashMap.put(s, al);
        }
        adapter = new ExpandableListAdapter(this, timeList, hashMap);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String track = returnCursor.getString(nameIndex);
                popChooser(track, uri.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void popChooser(final String track, final String uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"Pick New Time", "Pick from current list"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickNewTime(track, uri);
                } else {
                    pickFromCurrent(track, uri);
                }
            }
        });
        builder.show();
    }

    private void pickFromCurrent(final String track, final String uri) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ListView lv = new ListView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lv.setLayoutParams(lp);
        final int[] p = {0};
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                p[0] = position;
                if (row != null) {
                    row.setBackgroundColor(Color.WHITE);
                }
                row = view;
                view.setBackgroundColor(Color.CYAN);
            }
        });
        lv.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, timeList));
        builder.setView(lv);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String time = timeList.get(p[0]);
                String s = myPrefs.getString(day + time, "");
                s += track + SPLITTER;
                editor.putString(day + time, s);
                editor.putString(track, uri);
                editor.apply();
                updateList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void pickNewTime(final String track, final String uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final TimePicker tp = new TimePicker(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        tp.setLayoutParams(lp);
        builder.setView(tp);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String time = getAmPmTime(tp.getCurrentHour(), tp.getCurrentMinute());
                if (!timeList.contains(time)) {
                    timeList.add(time);
                    String s = myPrefs.getString(day, "");
                    s += time + SPLITTER;
                    editor.putString(day, s);
                    editor.apply();
                }
                String s = myPrefs.getString(day + time, "");
                s += track + SPLITTER;
                editor.putString(day + time, s);
                editor.putString(track, uri);
                editor.apply();
                addSchedule(time);
                updateList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void addSchedule(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, toggled);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0,2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3)));
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, (int)calendar.getTimeInMillis(), intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
        Log.d("AlarmManager: ", alarmManager.toString());
    }

    private String getAmPmTime(Integer currentHour, Integer currentMinute) {
        return String.format("%02d:%02d", currentHour, currentMinute);
    }


    public void playMusic(String time){
        for(MediaPlayer m : mps)
        {
            if(m.isPlaying())
            {
                m.stop();
                m.release();
            }
        }
        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        ArrayList<Uri> uris = new ArrayList<>();
        mps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String d = "";
        switch(calendar.get(Calendar.DAY_OF_WEEK)){
            case 1: d = SUNDAY;
                break;
            case 2: d = MONDAY;
                    break;
            case 3: d = TUESDAY;
                break;
            case 4: d = WEDNESDAY;
                break;
            case 5: d = THURSDAY;
                break;
            case 6: d = FRIDAY;
                break;
            case 7: d = SATURDAY;
                break;
            default: break;
        };

        String s = myPrefs.getString(d + time, "");
        for(String x: s.split(SPLITTER))
        {
            if(!x.trim().equals(""))
            {
                String z = myPrefs.getString(x,"");
                if(!z.trim().equals(""))
                {
                    Uri uri = Uri.parse(z);
                    uris.add(uri);
                    Log.d("URI: ", uri.toString());
                }
            }
        }
        for(Uri u: uris){
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                mp.setDataSource(this, u);
                mp.prepare();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mps.add(mp);
        }
        for(int i = 0; i < mps.size()-1; i++)
        {
            mps.get(i).setNextMediaPlayer(mps.get(i+1));
        }
        if(mps.size() >= 1){
            mps.get(0).start();
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private ArrayList<String> _listDataHeader;
        private HashMap<String, ArrayList<String>> _listDataChild;

        public ExpandableListAdapter(Context context, ArrayList<String> listDataHeader,
                                     HashMap<String, ArrayList<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblListItem);

            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
