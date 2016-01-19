package com.adnet.frettmul;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    ObservableVideoView vvMain;
    RelativeLayout llMain;
    Activity mContext;
    MediaController mc;
    TextView tvCurTime;
    TextView tvNotice;
    ToggleButton tgSwitch;
    Button   btStartLoop;
    Button   btEndLoop;
    TextView tvStartTime;
    TextView tvEndTime;
    boolean bStartCheckFlag = false;        ///Flag that current time is passed start time.
    boolean bEndCheckFlag = false;          ///Flag that current time is passed end time.

    Hashtable lstTimeText = new Hashtable();
    int[] arrayKeys;
    int currentTime = 0;                ////Now playing time.
    int durationTime = 0;               ///Video duration
    int startPos = 0;                   ///start point of loop
    int endPos = 0;                     ///end point of loop
    boolean  mbLoopable = false;        ///flag of checking loop
    boolean mbPlaying = true;           ///Flag of now playing.
    Uri videoUri;
    private Handler mCurTimeShowHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);
        mContext = this;
        initUI();       ///Init UI(textView, VideoView....)
        initTxt();      ///Read titles from text resource.
    }
    void getStartEndTime()      ///Set startPos and endPos from TextView of tvStartTime and tvEndTime
                                ///Convert String data of TextView to Integer data.
    {
        if(tvStartTime.getText().toString().length() != 0)
            startPos = Integer.parseInt(tvStartTime.getText().toString());
        else
            startPos = 0;
        if(tvEndTime.getText().toString().length() != 0)
            endPos = Integer.parseInt(tvEndTime.getText().toString());
        else
            endPos = 0;
    }
    private void initUI() {

        tvCurTime = (TextView)findViewById(R.id.tvCurTime);     ///TextView that shows current time.
        tvNotice = (TextView) findViewById(R.id.tvNotice);      ///TextView that shows title.

        llMain = (RelativeLayout)findViewById(R.id.llVideoView);
        tgSwitch = (ToggleButton)findViewById(R.id.tgSwitch);   ///ToggleButton that sets loop.
        btStartLoop = (Button)findViewById(R.id.btnStartLoop);  ///Button that sets startTime while playing video.
        btEndLoop = (Button)findViewById(R.id.btnEndLoop);      ///Button that sets endTime while playing video.
        tvStartTime = (TextView)findViewById(R.id.tvStartTime);
        tvEndTime = (TextView)findViewById(R.id.tvEndTime);
        tgSwitch.setChecked(false);
        tvStartTime.setText("0");
        tvEndTime.setText("0");
        tvCurTime.setText("0");
        videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test_video);
        vvMain = (ObservableVideoView)findViewById(R.id.vvMain);
        vvMain.setVideoURI(videoUri);
        ////New MediaController including VideoView. It shows timeline and play and forward,
        // backward button on the VideoView.
        mc = new MediaController(vvMain.getContext());
        mc.setMediaPlayer(vvMain);
        mc.setAnchorView(llMain);

        vvMain.setMediaController(mc);
        vvMain.start(); ///Play Video
        durationTime = vvMain.getDuration(); //Get video duration.

        ////This is runnable thread that sets currentTime to tvCurTime TextView and check loop
        // available through startPos and endPos

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                {
                    ///Set currentTime to current time textview.
                    currentTime = vvMain.getCurrentPosition();
                    tvCurTime.setText(String.valueOf(currentTime));
                    ///Set the current title of current time.
                    changeText(currentTime);

                    if(mbLoopable){
                        if(startPos >= endPos) {
                            Toast.makeText(mContext, "Start time is bigger than End time.", Toast.LENGTH_LONG).show();
                            mbLoopable = false;
                            tgSwitch.setChecked(false);
                        }else{
                            ///if currentTime is smaller than startPos then set bStartCheckFlag
                            // true.
                            //and set endChekFlag to false. Set current pos to startPos. and loop
                            // video.
                            if((currentTime < startPos) && (!bStartCheckFlag)){
                                vvMain.seekTo(startPos);
                                bStartCheckFlag = true;
                                bEndCheckFlag = false;
                            }

                            ///if currentTime is bigger than startPos then set bEndCheckFlag
                            // true.
                            //and set startChekFlag to false.
                            ///Set current pos to startPos. and loop video.
                            if((currentTime > endPos) && (!bEndCheckFlag)){
                                bEndCheckFlag = false;
                                bStartCheckFlag = true;
                                vvMain.seekTo(startPos);
                            }
                        }
                    }
                    mCurTimeShowHandler.postDelayed(this, 1);
                }
            }
        };
        mCurTimeShowHandler.post(runnable);

        ////This part is set startpos and endpos while editing startTime EditBox and endTime
        // EditBox.
//        tvStartTime.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
////                if(tvStartTime.getText().toString().length() == 0){
////                        tvStartTime.setText("0");
////                }
//                getStartEndTime();
//            }
//        });

//        tvEndTime.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(tvEndTime.getText().toString().length() != 0){
//                     if(Integer.parseInt(tvEndTime.getText().toString()) >= vvMain.getDuration()){
//                        Toast.makeText(mContext, "EndTime is bigger than video duration.", Toast.LENGTH_LONG).show();
//                        tvEndTime.setText("0");
//                    }
//                }
//                getStartEndTime();
//            }
//        });

        ///When press switch, change the TextView color of startTime and endTime
        ///Set loopable flag.
        tgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mbLoopable) {
                    tvStartTime.setTextColor(Color.parseColor("#000000"));
                    tvEndTime.setTextColor(Color.parseColor("#000000"));
                    mbLoopable = false;
                } else {
                    ///check current time is in duration of startPosition and endPosition.
                    String strStartPos = tvStartTime.getText().toString();
                    String strEndPos = tvEndTime.getText().toString();
                    int nTmpStartPos, nTmpEndPos;
                    if(strStartPos.length() != 0)
                        nTmpStartPos = Integer.parseInt(strStartPos);
                    else {
                        nTmpStartPos = 0;
                        tvStartTime.setText("0");
                    }
                    if(strEndPos.length() != 0)
                        nTmpEndPos = Integer.parseInt(strEndPos);
                    else {
                        nTmpEndPos = 0;
                        tvEndTime.setText("0");
                    }
                    ///if start position is bigger than end position then can not loop.
                    if (nTmpStartPos >= nTmpEndPos) {
                        tvStartTime.setTextColor(Color.parseColor("#000000"));
                        tvEndTime.setTextColor(Color.parseColor("#000000"));
                        mbLoopable = false;
                        Toast.makeText(mContext, "Start time is bigger than End time.", Toast.LENGTH_LONG).show();
                        tgSwitch.setChecked(false);
                    } else {
                        tvStartTime.setTextColor(Color.parseColor("#FF0000"));
                        tvEndTime.setTextColor(Color.parseColor("#0000FF"));
                        getStartEndTime();
                        if ((currentTime < startPos) || (currentTime > endPos)) {
                            currentTime = startPos;
                            tvCurTime.setText(String.format("%d", currentTime));
                            vvMain.seekTo(startPos);
                        }
                        bStartCheckFlag = false;
                        bEndCheckFlag = false;
                        mbLoopable = true;
                    }
                }
            }
        });
///Set startPosition of video by pressing "START LOOP" Button
        btStartLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPos = Integer.parseInt(tvCurTime.getText().toString());
                tvStartTime.setText(String.format("%d", startPos));
            }
        });
///Set endPosition of video by pressing "END LOOP" Button
        btEndLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPos = Integer.parseInt(tvCurTime.getText().toString());
                tvEndTime.setText(String.format("%d", endPos));
            }
        });

        /////This is videoview listener.
        vvMain.setVideoViewListener(new ObservableVideoView.IVideoViewActionListener() {
            @Override
            public void onPause() {
                mbPlaying = false;
                ////if press pause button then stop loop.
                mCurTimeShowHandler.removeCallbacks(runnable);
            }

            @Override
            public void onResume() {
                mbPlaying = true;
                getStartEndTime();
                ////if press resume button then start loop
                mCurTimeShowHandler.post(runnable);
            }

            @Override
            public void onTimeBarSeekChanged(int currentTime) {
                if (mbLoopable) {
                    ///When current time is setted from seeking timeline,
                    ///If current time is smaller than start position then set bStartChekFlag false.
                    ///current time is smaller than start position, compare start position and
                    // current time in thread.it must check in thread.
                    ///Also sets bEndCheckFlag false when current time is bigger than end position.
                    ///So the thread have to compare between end position and current time.
                    if (currentTime < startPos)
                        bStartCheckFlag = false;
                    else if (currentTime > endPos)
                        bEndCheckFlag = false;
                    else
                    {
                        ///When current time is in the duration of loop.
                        bStartCheckFlag = true;
                        bEndCheckFlag = false;
                    }
                }
                //Set the current time of textview and change the text of current timee while
                // seeking the timeline.
                tvCurTime.setText(String.format("%d", currentTime));
                changeText(currentTime);
            }

        });

    }


    ///Read from text source and get the array of time and its text.
    public void initTxt()
    {
        String str= readRawTextFile( this.getBaseContext(), R.raw.test_data );
        String[] strArray = str.split( "\n" );
        for( int nIndex= 0; nIndex < strArray.length; nIndex++ )
        {
            ///Split the every line of source text to two parts.
            // Every line is splited by ' ',
            String[] strArrTemp = strArray[nIndex].split(" ");
            String strTime = strArrTemp[0];     ///This is time
            String strText = strArrTemp[1];     // This is text of that strTime.
            ///If ther's same time, then add two text to hashtable.
            // else add one text of the time to hashtable.
            if(lstTimeText.containsKey(Integer.parseInt(strTime)))
            // if there's same key in the
            // hashtable then add other text of same time.
            {
                String strTemp = (String)lstTimeText.get(Integer.parseInt(strTime));
                lstTimeText.put(Integer.parseInt(strTime), strTemp + ":" + strText);
            }else
                lstTimeText.put(Integer.parseInt(strTime), strText);

        }
        ///save the key array of hashtable to int array.
        arrayKeys = new int[lstTimeText.size()];
        int i = 0;
        for ( Enumeration e = lstTimeText.keys(); e.hasMoreElements(); ) {
            arrayKeys[i] = (int) e.nextElement();
            i++;
        }
        Arrays.sort(arrayKeys);
    }
    ///Read the text file from resource(Raw) and divide by end line mark('\n")
    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
    ///changeText(int currentTime)
    ///Set the text of currentTime.
    public void changeText(int currentTime)
    {
        //From the first to number of hashtable keys, Search index that its value is bigger than
        // current time. Then sets the text that was finded in hashtable keys.
        for ( int nIndex = 0; nIndex < arrayKeys.length; nIndex++ )
        {
            if( arrayKeys[nIndex] == currentTime) {
                tvNotice.setText( (String) lstTimeText.get( arrayKeys[nIndex] ));
            } else if ( arrayKeys[nIndex] < currentTime && !(nIndex == arrayKeys.length))
            {
                tvNotice.setText((String) lstTimeText.get(arrayKeys[nIndex]));
            }
        }
    }

}
