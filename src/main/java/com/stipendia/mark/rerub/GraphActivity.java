package com.stipendia.mark.rerub;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.stipendia.mark.rerub.HandleXML.*;

public class GraphActivity extends Activity{

    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private TimeSeries mCurrentSeries1;
    private TimeSeries mCurrentSeries2;

    private LinearLayout layout;
    private void initChart(){


        mCurrentSeries1 = new TimeSeries("USD");
        mCurrentSeries2 = new TimeSeries("EUR");
        generateRates();
        for (int i = 0; i < getDayPeriod().length; i++) {
            mCurrentSeries1.add(getDayPeriod(i), 13500/getEurPeriod(i));
            mCurrentSeries2.add(getDayPeriod(i), 13500/getUsdPeriod(i));
        }
        mDataset.addSeries(mCurrentSeries1);
        mDataset.addSeries(mCurrentSeries2);

        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        XYSeriesRenderer renderer2 = new XYSeriesRenderer();
        renderer1.setColor(Color.RED);
        renderer1.setLineWidth(2);
        renderer1.setPointStyle(PointStyle.SQUARE);
        renderer1.setFillPoints(true);

//        renderer2.setDisplayChartValues(true);
        renderer2.setColor(Color.BLUE);
        renderer2.setLineWidth(2);
        renderer2.setPointStyle(PointStyle.SQUARE);
        renderer2.setFillPoints(true);


        mRenderer.addSeriesRenderer(renderer1);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.setGridColor(Color.WHITE);
//        mRenderer.setMarginsColor(Color.TRANSPARENT);


        /*
        * Settings for different screen resolutions;
        * */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        mRenderer.setZoomEnabled(false);
        mRenderer.setExternalZoomEnabled(true);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0,Color.BLACK);
        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
//        mRenderer.setPanEnabled(true, false); //ScrollX = true; ScrollY = false
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if (size.y < 600){
                mRenderer.setMargins(new int[]{10, 10, -10, 10});//{top,left,bot,right}
                mRenderer.setYLabels(10);
            } else {
                mRenderer.setMargins(new int[]{30, 20, 20, 20});//{top,left,bot,right}
                mRenderer.setYLabels(10);
                mRenderer.setLabelsTextSize(25);
                mRenderer.setLegendTextSize(30);
                renderer1.setChartValuesTextSize(25);
                mRenderer.setYAxisMin(13500/getMinMoneyRate()-100);
                mRenderer.setYAxisMax(13500/getMaxMoneyRate()+100);
                mRenderer.setXAxisMin(HandleXML.getDayPeriod(0).getTime());
                mRenderer.setXAxisMax(HandleXML.getDayPeriod(HandleXML.getDayPeriod().length-1).getTime());
            }
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mRenderer.setMargins(new int[]{30,30,20,30});
            mRenderer.setFitLegend(false);
            mRenderer.setYLabels(10);
            mRenderer.setLabelsTextSize(25);
            mRenderer.setLegendTextSize(30);
            renderer1.setChartValuesTextSize(25);

            p.addRule(RelativeLayout.ABOVE, R.id.todayIs);

            layout.setLayoutParams(p);
        }



        /* [panMinimumX, panMaximumX, panMinimumY, panMaximumY] */
        double[] panLimits={HandleXML.getDayPeriod(0).getTime()-1,
                HandleXML.getDayPeriod(HandleXML.getDayPeriod().length-1).getTime()+1,
                getMinMoneyRate()-5,getMaxMoneyRate()+5};
        mRenderer.setPanLimits(panLimits);
        mRenderer.setShowGrid(true);
//        mRenderer.setZoomButtonsVisible(true);
//        mRenderer.setZoomEnabled(true);
//        mRenderer.setZoomRate(2);

    }


    protected void onResume() {
        super.onResume();
        if (mChart == null) {
            initChart();
            mChart = ChartFactory.getTimeChartView(this,mDataset,mRenderer,"dd.MM.yy");

            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        TextView tv = (TextView)findViewById(R.id.todayIs);
        layout = (LinearLayout) findViewById(R.id.chart);
        String path= "Graph Activity";
        Tracker t = ((Analytics) getApplication()).getTracker(
                Analytics.TrackerName.APP_TRACKER);       // Get tracker.
        t.setScreenName(path);     // Pass a String representing the screen name.
        t.send(new HitBuilders.AppViewBuilder().build());       // Send a screen view.

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = df.format(c.getTime());
        tv.setText(tv.getText()+formattedDate);
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
                        changeActivityToMain(this.findViewById(R.id.graphActivity));
                    if (x1 > x2)
                        changeLayoutToInfo(this.findViewById(R.id.graphActivity));
                }
                break;
            }
        }
        return false;
    }

    public void changeActivityToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
        finish();
    }
    public void changeLayoutToInfo(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_left);
        finish();
    }


    public void zoomIn(View view) {
        mChart.zoomIn();

    }

    public void zoomOut(View view) {
        mChart.zoomOut();
    }

    public void zoomHome(View view) {
        mChart.zoomReset();
    }
}
