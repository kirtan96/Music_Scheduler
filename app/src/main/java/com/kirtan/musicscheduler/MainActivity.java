package com.kirtan.musicscheduler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected SharedPreferences myPrefs;
    protected SharedPreferences.Editor editor;
    protected HashMap<String, ArrayList<String>> hashMap;
    protected ArrayList<String> timeList;
    protected final String ALL_TIMES = "allTimes";
    protected final String SPLITTER = "/////";
    protected ExpandableListView listView;
    protected ExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ExpandableListView) findViewById(R.id.listView);

        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();

        updateList();

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
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    showDelete(groupPosition, childPosition);


                    return true;
                }
                else if(ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    //TODO: longClick on group
                }

                return false;
            }
        });
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
        editor.putString(s, tmp).apply();
        updateList();
    }

    private void updateList() {
        timeList = new ArrayList<>();
        hashMap = new HashMap<>();
        for (String s : myPrefs.getString(ALL_TIMES, "").split(SPLITTER)) {
            if (!s.trim().equals("")) {
                timeList.add(s);
            }
        }
        Collections.sort((List) timeList);
        for (String s : timeList) {
            ArrayList<String> al = new ArrayList<>();
            for (String x : myPrefs.getString(s, "").split(SPLITTER)) {
                al.add(x);
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
                popChooser(track);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void popChooser(final String track) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"Pick New Time", "Pick from current list"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickNewTime(track);
                } else {
                    pickFromCurrent(track);
                }
            }
        });
        builder.show();
    }

    private void pickFromCurrent(final String track) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ListView lv = new ListView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lv.setLayoutParams(lp);
        final int[] p = {0};
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                p[0] = position;
            }
        });
        lv.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, timeList));
        builder.setView(lv);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String time = timeList.get(p[0]);
                String s = myPrefs.getString(time, "");
                s += track + SPLITTER;
                editor.putString(time, s);
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

    private void pickNewTime(final String track) {
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
                    String s = myPrefs.getString(ALL_TIMES, "");
                    s += time + SPLITTER;
                    editor.putString(ALL_TIMES, s);
                    editor.apply();
                }
                String s = myPrefs.getString(time, "");
                s += track + SPLITTER;
                editor.putString(time, s);
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

    private String getAmPmTime(Integer currentHour, Integer currentMinute) {
        return String.format("%02d:%02d", currentHour, currentMinute);
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
