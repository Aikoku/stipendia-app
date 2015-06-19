package com.stipendia.mark.rerub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.Format;
import java.text.SimpleDateFormat;

import static com.stipendia.mark.rerub.HandleXML.dateList;
import static com.stipendia.mark.rerub.HandleXML.eurList;
import static com.stipendia.mark.rerub.HandleXML.roundUsd;


public class MainActivity extends ActionBarActivity {

    float x1, x2;
    float y1, y2;

    private EditText tempRub, eurRate, usdRate, totalEur, totalUsd;
    private double totalRub;

    private class DownloadXmlTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
                // Output stream to write file in SD card
                OutputStream output = new FileOutputStream(
                        getCacheDir() + "/90day.xml");
//                Log.e("RubRes","dir = "+Environment.getExternalStorageDirectory().getPath());
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }
    }

    public void open() {
//        String rssUrl = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        String rssUrl = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
        HandleXML obj = new HandleXML(rssUrl);
        obj.fetchXML();
        while (obj.parsingComplete) ;
    }


    /**
     * Function to display simple Alert Dialog
     *
     * @param context - application context
     * @param title   - alert dialog title
     * @param message - alert message
     * @param status  - success/failure (used to set icon)
     */
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }


    //    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        String path = "Main Activity";
        Tracker t = ((Analytics) getApplication()).getTracker(
                Analytics.TrackerName.APP_TRACKER);       // Get tracker.
        t.setScreenName(path);     // Pass a String representing the screen name.
        t.send(new HitBuilders.AppViewBuilder().build());       // Send a screen view.

/*

        Tracker t = //Get a Tracker (should auto-report)
                ((Analytics) getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName(path);
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
*/
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();//true or false
        this.setContentView(R.layout.activity_main);


        if (isInternetPresent) {
            String rssUrl = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
            HandleXML obj = new HandleXML(rssUrl);
            obj.fetchXML();
            while (obj.parsingComplete) ;
            //Download xml and store it in cache
            new DownloadXmlTask().execute(rssUrl);
        } else {
            showAlertDialog(MainActivity.this, "Нет сети",
                    "Вы не подключены к интернету, данные могли устареть.", false);
            String rssPath = getCacheDir() + "/90day.xml";
            HandleXML obj = new HandleXML(rssPath);
            obj.fetchOfflineXML(getCacheDir() + "/90day.xml");
            while (obj.parsingComplete) ;
        }


        tempRub = (EditText) findViewById(R.id.editText);
        eurRate = (EditText) findViewById(R.id.eurRate);
        usdRate = (EditText) findViewById(R.id.usdRate);
        totalEur = (EditText) findViewById(R.id.totalEur);
        totalUsd = (EditText) findViewById(R.id.totalUsd);
        TextView lastUpdateDate = (TextView) findViewById(R.id.lastUpdateDate);

        File file = new File(getCacheDir() + "/totalRub");
        Format formatter = new SimpleDateFormat("dd.MM.yyyy");
        if (file.exists()) {
            tempRub.setText(ReadTotalRub());
            lastUpdateDate.setText(lastUpdateDate.getText() + formatter.format(dateList.get(0)));
//            Log.e("RubRes", "NOT FIRST = " + ReadTotalRub());
        } else {
            InitTotalRub("13500");
            tempRub.setText(ReadTotalRub());
            lastUpdateDate.setText(lastUpdateDate.getText() + formatter.format(dateList.get(0)));
//            Log.e("RubRes", "FIRST START = " + ReadTotalRub());
        }
        totalRub = Double.parseDouble(String.valueOf(tempRub.getText()));

        eurRate.setText("" + eurList.get(0));
        usdRate.setText("" + roundUsd(0));
//        Log.e("test", 1+") eurList = "+ dateList.get(1) + " EUR = "+ eurList.get(1)+ " USD = "+ usdList.get(1));
        refreshedTotalRub();

        ImageButton imgBtn = (ImageButton) findViewById(R.id.imageButton);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshedTotalRub();
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(tempRub.getWindowToken(), 0);
                Tracker t = ((Analytics) getApplication()).getTracker(
                        Analytics.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Refresh Event")
                        .setAction("Ruble Btn Pressed")
                        .build());

            }
        });

        tempRub.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    refreshedTotalRub();
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(tempRub.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
//        deleteCache(this);
    }


    protected void InitTotalRub(String content) {
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(getCacheDir() + "/totalRub");
            Log.e("RubRes", "InitCache = " + content);

            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String ReadTotalRub() {
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir() + "/totalRub"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
            Log.e("RubRes", "ReadCache = " + buffer.toString());
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void refreshedTotalRub() {
        EditText rub = (EditText) findViewById(R.id.editText);
        InitTotalRub(rub.getText().toString());
        Log.e("RubRes", "RefreshCache = " + ReadTotalRub());
        this.totalRub = Double.parseDouble(rub.getText().toString());
        double eur = totalRub / eurList.get(0);
        double usd = totalRub / roundUsd(0);

        double rounded = (double) Math.round(eur * 1000) / 1000;
        totalEur.setText(String.valueOf(rounded) + " €");

        rounded = (double) Math.round(usd * 1000) / 1000;
        totalUsd.setText(String.valueOf(rounded) + " $");

    }

    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN: {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = touchevent.getX();
                y2 = touchevent.getY();
                if (Math.abs(x2 - x1) > 100) {
                    //left > right
                    if (x1 < x2)
                        changeLayoutToInfo(this.findViewById(R.id.mainActivity));
                    // right > left
                    if (x1 > x2)
                        changeLayoutToGraph(this.findViewById(R.id.mainActivity));
                }
                break;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeLayoutToInfo(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        finish();
    }


    public void changeLayoutToGraph(View view) {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        finish();
    }


    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
