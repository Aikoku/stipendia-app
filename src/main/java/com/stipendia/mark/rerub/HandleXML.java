package com.stipendia.mark.rerub;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HandleXML {
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    private double eurRate = 0;
    private String eurRateStr = null;
    private double usdRate = 0;
    public volatile boolean parsingComplete = true;
    public static Date[] dayPeriod;
    public static Double[] eurPeriod;
    public static Double[] usdPeriod;
    public static ArrayList<Date> dateList = new ArrayList<Date>();
    public static ArrayList<Double> eurList = new ArrayList<Double>();
    public static ArrayList<Double> usdList = new ArrayList<Double>();

    public static Double getEurPeriod(int i) {
        return eurPeriod[i];
    }

    public static Double getUsdPeriod(int i) {
        return usdPeriod[i];
    }

    public static Date[] getDayPeriod() {
        return dayPeriod;
    }

    public HandleXML(String url) {
        this.urlString = url;
    }

    public static Date getDayPeriod(int i) {
        return dayPeriod[i];
    }

    public static int getMaxMoneyRate(){
        double max = eurPeriod[0];
        for (int i = 1; i < eurPeriod.length; i++)
        {

                if (eurPeriod[i] > max) {
                    max = eurPeriod[i];
                    if(max < usdPeriod[i])
                        max = usdPeriod[i];
                }
        }
        return (int)max;
    }
    public static int getMinMoneyRate(){
        double min = eurPeriod[0];
        for (int i = 1; i < eurPeriod.length; i++)
        {
            if (eurPeriod[i] < min)
            {
                min = eurPeriod[i];
                if(min > usdPeriod[i])
                    min = usdPeriod[i];
            }
        }
        return (int)min;
    }


    /**
     * Generate Dates for GraphViewer
     *
     */
    public static void generateRates(/*int period*/) {
        // generate Rates for Dates
//        switch (period) {
//            case (1)://3 month's
                dayPeriod= new Date[dateList.size()];//This will get ~60 points
                eurPeriod=new Double[dayPeriod.length];
                usdPeriod = new Double[dayPeriod.length];
                for (int i = 0,j=dayPeriod.length-1;i<dayPeriod.length;i++,j--){

//                    Log.e("RubRes",i+") lastUpdateDate  = "+dateList.get(i));
                    dayPeriod[i] = dateList.get(j);
                    eurPeriod[i] = eurList.get(j);
                    usdPeriod[i] = roundUsd(j);
                }
    }
    public static double roundUsd (int i){
        double rounded = 0.0;
        rounded = (double) Math.round(eurList.get(i)/usdList.get(i)*1000)/1000;
        return rounded;
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;

        int count = 0;
        try {
            event = myParser.getEventType();

//            Log.e("RubRes","event ="+event);
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
//                Log.e("RubRes","xmlName ="+name);
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (myParser.getAttributeCount()==1){
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = format.parse(myParser.getAttributeValue(0));
                            dateList.add(date);
//                            Log.e("FetchXml",count+ ") time = "+date);
                            count++;
                        }
                        if (name.equals("Cube")) {
                            String cube = myParser.getAttributeValue(null, "currency");
                            if (cube != null) {
                                if (cube.equals("USD")) {
                                    String usdRateStr = myParser.getAttributeValue(null, "rate");
                                    usdRate = Double.parseDouble(usdRateStr);
                                    usdList.add(usdRate);
//                                    Log.e("FetchXml", count + ") usd = " + usdRate);
                                }
                                if (cube.equals("RUB")) {
                                    eurRateStr = myParser.getAttributeValue(null, "rate");
                                    eurRate = Double.parseDouble(eurRateStr);
                                    eurList.add(eurRate);
//                                    Log.e("FetchXml", count + ") eur = " + eurRate);
                                }
                            }
                        } else {
                            break;
                        }

                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = myParser.next();
            }
            parsingComplete = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fetchXML() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)
                            url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    Log.e("RubRes","stream  = "+stream);
                    Log.e("RubRes","mparser  = "+myparser);
                    parseXMLAndStoreIt(myparser);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public void fetchOfflineXML(final String rssPath) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

//                    File file = new File(rssPath);
                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(new StringReader(readFileAsString(rssPath)));

                    parseXMLAndStoreIt(myparser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }
    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }



}
