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

public class InfrParser {


    //private ArrayList<LatLng> allTracks;

    public InfrParser() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ArrayList<LatLng> getCoordinateArrays(InputStream stream) {
        //ArrayList<LatLng> allPlaces = new ArrayList<LatLng>();
        ArrayList<LatLng> onePlace = new ArrayList<LatLng>();

        try {
            StringBuilder buff = new StringBuilder();

            InputStream json = stream;
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String string;
            String buffer;
            while ((string = in.readLine()) != null) {
                buff.append(string);
            }

            in.close();
            String html = buff.toString();
            Document doc = Jsoup.parse(html, "", Parser.xmlParser());
            ArrayList<String> tracksString = new ArrayList<String>();

            for (Element f : doc.select("latitude")) {
                System.out.println("Latitude:" + f.toString().replace("<latitude>", "").replace("</latitude>", ""));
                tracksString.add(f.toString().replace("<latitude>", "").replace("</latitude>", ""));
            }

            for (Element e : doc.select("longitude")) {
                System.out.println("Longitude:" + e.toString().replace("<longitude>", "").replace("</longitude>", ""));
                tracksString.add(e.toString().replace("<longitude>", "").replace("</longitude>", ""));
            }

            //for (int i = 0; i < tracksString.size(); i++) {
            LatLng latLng = new LatLng(Double.parseDouble(tracksString.get(0)),
                    Double.parseDouble(tracksString.get(1)));
            //ArrayList<String> onePlaceString = new ArrayList<String>(Arrays.asList(tracksString.get(i).split("\\s+")));
            //for (int k = 1; k < onePlaceString.size(); k++) {
            //LatLng latLng = new LatLng(Double.parseDouble(onePlaceString.get(k).split(",")[0]),
            //Double.parseDouble(onePlaceString.get(k).split(",")[1]));
            // onePlace.add(latLng);
            // }
            ArrayList<LatLng> oneTrack = new ArrayList<LatLng>();
            oneTrack.add(latLng);
            // allTracks.add(oneTrack);

            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* System.out.println(allTracks);
        return allTracks;*/
        return onePlace;
    }
}




       //} catch (Exception e) {
           // e.printStackTrace();
        //}
       // System.out.println(onePlace);
        //return onePlace;


   /* public ArrayList<String> getNamesArrays(InputStream stream) {
        ArrayList<String> namesString = new ArrayList<String>();
        try {
            StringBuilder buffe = new StringBuilder();

            InputStream json = stream;
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String string;
            String buffer;
            while ((string = in.readLine()) != null) {
                buffe.append(string);
            }

            in.close();
            String html = buffe.toString();
            Document doc = Jsoup.parse(html, "", Parser.xmlParser());

            for (Element e : doc.select("SimpleData")) {
                if(e.attr("name").equalsIgnoreCase("TYPE")){
                    System.out.println(e.toString());
                    namesString.add(e.toString().replace("<simpledata name=\"TYPE\">", "").replace("</simpledata>", ""));
                }
            }
        } catch (Exception f)
        {
            f.printStackTrace();
        }

        return namesString;

    }
}*/


