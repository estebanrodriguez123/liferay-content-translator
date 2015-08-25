
package com.rivetlogic.translator.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Translate Util
 * Using the AJAX Interface V2 - see: http://msdn.microsoft.com/en-us/library/ff512404.aspx
 * @author andreslizano
 */
public class TranslatorAPI {

    private static final String ENCODING = "UTF-8";
    private static final String DAUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    private static final String TranslateURL = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?";
    private static final String TranslateAarrayURL = "http://api.microsofttranslator.com/V2/Ajax.svc/TranslateArray?";

    private static String clientId = "CLIENT_ID";
    private static String clientSecret = "CLIENT_SECRET";
    
    private static String token;
    private static long tokenExpiration = 0;
    
    /**
     * Gets the Client Id
     * @return clientID.
     */
    public static String getClientId() {
        return clientId;
    }
    
    /**
     * Sets the Client ID.
     * @param clientId The Client Id.
     */
    public static void setClientId(String clientId) {
        TranslatorAPI.clientId = clientId;
    }
    
    /**
     * Gets the Client Secret
     * @return clientSecret
     */
    public static String getClientSecret() {
        return clientSecret;
    }
    
    /**
     * Sets the Client Secret.
     * @param clientSecret The Client Secret.
     */
    public static void setClientSecret(String clientSecret) {
        TranslatorAPI.clientSecret = clientSecret;
    }
    
    /**
     * Reads an InputStream and returns its contents as a String.
     * @param inputStream The InputStream to read from.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    private String read(final InputStream inputStream) throws Exception {
    	StringBuilder outputBuilder = new StringBuilder();
    	try {
            if (inputStream != null) {
                String line;
    		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
    		while (null != (line = reader.readLine())){
                    // Microsoft Api prepend \uFEFF to every response, so remove that from the line
                    outputBuilder.append(line.replaceAll("\uFEFF", ""));
    		}
            }
    	} catch (Exception ex) {
    		throw new TranslatorException("[microsoft-translator-api] Error reading translation stream.", ex);
    	}
    	
    	return outputBuilder.toString();
    }
    /**
     * Gets the OAuth access token.
     * @return the OAUTH acces token
     */
    private String getToken() throws Exception {
        String parameters = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + URLEncoder.encode(this.clientId,ENCODING) + "&client_secret=" + URLEncoder.encode(this.clientSecret,ENCODING);
        URL url = new URL(DAUri);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=" + ENCODING);
        urlConnection.setRequestProperty("Accept-Charset",ENCODING);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(parameters);
        writer.flush();

        try {
            int responseCode = urlConnection.getResponseCode();
            final String result = read(urlConnection.getInputStream());
            if(responseCode!=200){
                throw new TranslatorException("Error from Microsoft Translator API: " + result);
            }
            return result;
        }finally{
            if(urlConnection!=null) {
                urlConnection.disconnect();
            }
        }
    }
    
    /**
     * Execute the url and return a JSON with the translation 
     * @return Translation (JSON format) 
     */
    private String executeUrl(URL url) throws Exception{
        if(clientId!=null&&clientSecret!=null&&System.currentTimeMillis()>tokenExpiration) {
            String tokenJson = getToken();
            Integer expiresIn = Integer.parseInt((String)((JSONObject)JSONValue.parse(tokenJson)).get("expires_in"));
            this.tokenExpiration = System.currentTimeMillis()+((expiresIn*1000)-1);
            this.token = "Bearer " + (String)((JSONObject)JSONValue.parse(tokenJson)).get("access_token");
        }
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type","html/plain; charset=" + ENCODING);
        urlConnection.setRequestProperty("Accept-Charset",ENCODING);
        if(this.token!=null)
            urlConnection.setRequestProperty("Authorization",this.token);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        try{
            int responseCode = urlConnection.getResponseCode();
            String result = read(urlConnection.getInputStream());
            if(responseCode != 200)
                throw new TranslatorException("Error from Microsoft Translator Api: "+ result);
            return result;
        }finally{
            if(urlConnection != null)
                urlConnection.disconnect();
        }
    }
    
    /**
     * Translate the text from source language to target language.
     * @param text Text to be translated.
     * @param from Source language.
     * @param to Target language.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    public String translate(String text, String from, String to) throws Exception{
        if(text.isEmpty()) {
            return text;
        }
        String params = "?"+""+"&from="+URLEncoder.encode(from,ENCODING)+"&to="+URLEncoder.encode(to,ENCODING)+"&text="+URLEncoder.encode(text,ENCODING);
        URL url = new URL(TranslateURL + params);
        try{
            String translationResponse = executeUrl(url);
            String translation = (String)JSONValue.parse(translationResponse);
            return translation.toString();
        }catch(Exception ex){

            throw new TranslatorException("[microsoft-translator-api] Error retrieving translation : " + ex.getMessage(), ex);
        }

    }
    
    /**
     * Auto-Detect the source language and translate the text to target language.
     * @param text Text to be translated.
     * @param to Target language.
     * @return The contents of the InputStream as a String.
     * @throws Exception on error.
     */
    public String translate(String text, String to) throws Exception{
        if(text.isEmpty()) {
            return text;
        }
        String params = "?"+""+"&from="+URLEncoder.encode(Languages.AUTO,ENCODING)+"&to="+URLEncoder.encode(to,ENCODING)+"&text="+URLEncoder.encode(text,ENCODING);
        URL url = new URL(TranslateURL + params);
        try{
            String translationResponse = executeUrl(url);
            String translation = (String)JSONValue.parse(translationResponse);
            return translation.toString();
        }catch(Exception ex){

            throw new TranslatorException("[microsoft-translator-api] Error retrieving translation : " + ex.getMessage(), ex);
        }

    }
    
    public ArrayList<String> multipleTranslation(String text, String from, String[] targets) throws Exception{
        ArrayList<String> translations=new ArrayList();
        for(String to:targets){
            try{
                translations.add(translate(text,from,to));
            }
            catch(Exception ex){
                throw new TranslatorException("[microsoft-translator-api] Error retrieving translation for the language ["+to+"] : " + ex.getMessage(), ex);
            }
        }
        return translations;
    }
    
    public ArrayList<String> multipleTranslation(String text, String[] targets) throws Exception {
        ArrayList<String> translations=new ArrayList();
        for(String to:targets){
            try{
                translations.add(translate(text,to));
            }
            catch(Exception ex){
                throw new TranslatorException("[microsoft-translator-api] Error retrieving translation for the language ["+to+"] : " + ex.getMessage(), ex);
            }
        }
       
        return translations;
    }
    
}
