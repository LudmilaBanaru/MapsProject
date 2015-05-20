package ch.unige.idsi.y15.parkfind_android;

import android.content.Context;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Studentka on 19.05.2015.
 */
public class ParserXML {

    // constructor
    public ParserXML() {

    }

    /**
     * Getting XML from URL making HTTP request
     * @param url string
     * */
    public InputStream getXmlFromUrl(String url) {
        String xml = null;
        Log.d("mytag", "getXmlFromUrl start...");
        Log.d("mytag", url);
        InputStream str = null;

        try {

            URL url_2 = new URL(url);
            URLConnection uc = url_2.openConnection();
            uc.connect();
            str = new BufferedInputStream(uc.getInputStream());
            //xml = convertStreamToString(str);
            // defaultHttpClient
           /* DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);*/

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return XML
        Log.d("mytag", "getXmlFromUrl end...");

        //Log.d("mytag",str);
        return str;
    }

    public String convertStreamToString(InputStream is) {
        Log.d("mytag", "convertStreamToString start...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("mytag", "convertStreamToString end...");
        return sb.toString();
    }

    public String readXMLFromFile(Context activity, String xmlFile)
    {
        InputStream is = null;
        File file = null;
        Writer writer = new StringWriter();
        file = new File(xmlFile);
        if (!(file == null))
        {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (is != null)
        {

            char[] buffer = new char[1024];
            try
            {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                is.close();
            }
            catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
        }

        return writer.toString();
    }

    public void writeXMLToFile(Context context, String xmlFile, String xmlData)
    {
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try
        {
            fOut = new FileOutputStream(new File(xmlFile));
            osw = new OutputStreamWriter(fOut);
            osw.write(xmlData);
            osw.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                osw.close();
                fOut.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getting XML DOM element
     *  XML string
     * */
    public Document getDomElement(String xml){
        Log.d("mytag", "getDomElement start...");
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);
        }
        catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        Log.d("mytag", "getDomElement end...");
        return doc;
    }

    /** Getting node value
     * @param elem element
     */
    public final String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * Getting node value
     *  Element node
     *  key string
     * */
    public String getValue(Element item, String str) {
        Log.d("mytag", "getValue start...");
        NodeList n = item.getElementsByTagName(str);
        Log.d("mytag", "getValue end...");
        return this.getElementValue(n.item(0));
    }

    public void setValue(Element elem, String str){
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        child.setNodeValue(str);
                    }
                }
            }
        }
    }

    public String GetElementAttribute(Element item, String attribName){
        return item.getAttribute(attribName);
    }
}