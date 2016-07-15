package com.kirtan.musicscheduler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Kirtan on 7/1/16.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver{

    SharedPreferences myPrefs;
    final String SPLITTER = "/////";
    ArrayList<Uri> uris;
    ArrayList<MediaPlayer> mps;

    @Override
    public void onReceive(final Context context, Intent intent) {
        //this will update the UI with message
        MainActivity inst =  MainActivity.instance();
        //inst.setAlarmText("Alarm! Wake up! Wake up!");

        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        /*Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();*/
        Log.d("Alarm", "Alarm is ringing!");


        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        String time = String.format("%02d:%02d", h, m);
        inst.playMusic(time);

        //this will send a notification message
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

    /*@Override
    public void onReceive(final Context context, Intent intent) {
        myPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        uris = new ArrayList<>();
        mps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        String time = String.format("%02d:%02d", h, m);
        String s = myPrefs.getString(time, "");
        for(String x: s.split(SPLITTER))
        {
            if(!x.equals(""))
            {
                String z = myPrefs.getString(x,"");
                if(!z.trim().equals(""))
                {
                    Uri uri = Uri.parse(z);
                    uris.add(uri);

                }
            }
        }
        for(Uri u: uris){
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                mp.setDataSource(context, u);
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
        if(mps.get(0) != null) {
            try {
                mps.get(0).prepare();
                mps.get(0).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }*/
}
