package com.stipendia.mark.rerub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * Created by Mark on 22.01.2015.
 */
public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView about_url1 = (TextView) findViewById(R.id.about_url1);
        TextView about_url3 = (TextView) findViewById(R.id.about_url3);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.x < 300){

            try {
                about_url1.setText("Программа создана для отслеживания\n" +
                        "        суммы стипендии, после перевода,\n" +
                        "        в реальном времени.\n" +
                        "        По умолчанию выставленна сумма стипендии мера Москвы,\n" +
                        "        но вы её можете легко изменить.\n" +
                        "        Более подробная информация\n" +
                        "        о стипендии Мера Москвы по ссылке:");
                about_url3.setText("Курс соответствуют ЕЦБ,\n" +
                        "        и он может отличатся от местных банков.\n" +
                        "         Автор: Марк Николаев");
                about_url3.setTypeface(null, Typeface.BOLD);
            }catch (Exception e){

                Log.e("RubRes","Error "+e);
            }
        /*
about_url1.setText("Программа создана для отслеживания\\n\n" +
        "        суммы стипендии, после перевода,\n" +
        "        в \\n реальном времени.\\n\n" +
        "        По умолчанию выставленна сумма \\n стипендии мера Москвы,\\n\n" +
        "        но вы её можете легко изменить.\\n\n" +
        "        Более подробная информация\\n\n" +
        "        о стипендии Мера Москвы по ссылке:");*/
            Log.e("RubRes","SMALL = "+size.y+" : " + size.x);
        }else{

            Log.e("RubRes","BIG = "+size.y+" : " + size.x);
        }

//        myLayout = (RelativeLayout) findViewById(R.id.aboutActivity);
//        TextView tv = (TextView) findViewById(R.id.textView7);
//        TextView tv = (TextView) findViewById(R.id.textView);
//        tv.setOnClickListener(new View.OnClickListener() {

//            @Override
//            public void onClick(View v) {
//                Intent ii = new Intent();
//                Toast toast = Toast.makeText(getApplicationContext(), "touch", Toast.LENGTH_SHORT);
//                toast.show();
//                ii.setClass(RoomForSpecificUser.this, DeviceOperation.class);
//                startActivity(ii);

//            }
//        });

//        ImageButton imgBtn = (ImageButton) findViewById(R.id.imageButton);
//        tv.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Toast toast = Toast.makeText(getApplicationContext(), "touch", Toast.LENGTH_SHORT);
//                toast.show();
//
//            }
//        });

//        tv.setOnEditorActionListener((TextView.OnEditorActionListener) this);
//        tv.setOnTouchListener(new TextView.OnTouchListener());
//        tv.setOnTouchListener(
//                new RelativeLayout.OnTouchListener() {
//                    public boolean onTouch(View v,
//                                           MotionEvent m) {
//                        onTouchEvent(m);
//                        return true;
//                    }
//                }
//        );

//        boolean b = myLayout.onTouchEvent(this);
//        myLayout.setOnTouchListener(
//                new RelativeLayout.OnTouchListener() {
//                    public boolean onTouch(View v,
//                                           MotionEvent m) {
//                        onTouchEvent(m);
//                        return true;
//                    }
//                }
//        );


        String path = "About Activity";
        Tracker t = ((Analytics) getApplication()).getTracker(
                Analytics.TrackerName.APP_TRACKER);       // Get tracker.
        t.setScreenName(path);     // Pass a String representing the screen name.
        t.send(new HitBuilders.AppViewBuilder().build());       // Send a screen view.

    }



    float x1,x2;
    float y1,y2;

    public boolean onTouchEvent(MotionEvent touchevent)
    {
        switch (touchevent.getAction())
        {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                if (Math.abs(x2 - x1) > 100) {
                    if (x1 < x2)
                        changeLayoutToGraph(this.findViewById(R.id.aboutActivity));
                    if (x1 > x2)
                        changeActivityToMain(this.findViewById(R.id.aboutActivity));
                }
                break;
            }
        }
        return true;
    }


    public void changeActivityToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_left);
                Log.e("RubRes","TO MAIN");
        finish();
    }
    public void changeLayoutToGraph(View view) {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
        Log.e("RubRes", "TO GRAPH");
        finish();
    }
}
