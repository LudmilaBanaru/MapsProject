package com.example.studentka.mapsproject;

import com.google.android.gms.maps.model.LatLng;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MyParser {

public MyParser(){

    super();
}

    public ArrayList<ArrayList<LatLng>> getCoordinateArrays(InputStream stream) {
        ArrayList<ArrayList<LatLng>> allTracks = new ArrayList<ArrayList<LatLng>>();

        try {
            StringBuilder buf = new StringBuilder();

            InputStream json = stream;
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String html = buf.toString();
            Document doc = Jsoup.parse(html, "", Parser.xmlParser());
            ArrayList<String> tracksString = new ArrayList<String>();



            for (Element f : doc.select("latitude")) {
                System.out.println("Latitude:"+f.toString().replace("<latitude>", "").replace("</latitude>", ""));
                tracksString.add(f.toString().replace("<latitude>", "").replace("</latitude>", ""));
            }

            for (Element e : doc.select("longitude")) {
                System.out.println("Longitude:"+e.toString().replace("<longitude>", "").replace("</longitude>", ""));
                tracksString.add(e.toString().replace("<longitude>", "").replace("</longitude>", ""));
            }
            for (Element f : doc.select("type")) {
                System.out.println("Type:"+f.toString().replace("<type>", "").replace("</type>", ""));
                tracksString.add(f.toString().replace("<latitude>", "").replace("</latitude>", ""));
            }


            LatLng latLng = new LatLng(Double.parseDouble(tracksString.get(0)),
                        Double.parseDouble(tracksString.get(1)));

                ArrayList<LatLng> oneTrack = new ArrayList<LatLng>();

                    oneTrack.add(latLng);

                allTracks.add(oneTrack);


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(allTracks);
        return allTracks;

    }

}