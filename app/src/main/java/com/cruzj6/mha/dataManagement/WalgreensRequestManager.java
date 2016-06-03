package com.cruzj6.mha.dataManagement;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.cruzj6.mha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joey on 5/23/16.
 */
public final class WalgreensRequestManager {

    private static RequestQueue reqQueue;

    private WalgreensRequestManager()
    {

    }

    public static void requestLandingURL(Context context, Response.Listener<WalLandingRespContainer> listener)
    {
        MakeReqQueue(context);

        JSONObject jsonObj = null;
        try {

            jsonObj = new JSONObject();
            jsonObj.put("apiKey", context.getString(R.string.walgreens_api_key));
            jsonObj.put("affId", context.getString(R.string.walgreens_affid));
            jsonObj.put("transaction", "refillByScan");
            jsonObj.put("act", "mweb5Url");
            jsonObj.put("view", "mweb5UrlJSON");
            jsonObj.put("devinf", "Android,5.0");
            jsonObj.put("appver", "1.0");

            String jsonString = jsonObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LandingRequestJson lr = new LandingRequestJson(context, jsonObj, listener, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        reqQueue.add(lr);
        reqQueue.start();
    }

    public void requestRxRefill(Context context, WalLandingRespContainer landCon)
    {

    }

    private static void MakeReqQueue(Context context)
    {
        if(reqQueue == null)
            reqQueue = Volley.newRequestQueue(context);
    }

}


class LandingRequestJson extends JsonRequest<WalLandingRespContainer> {

    private Response.ErrorListener errList;
    private Response.Listener listener;
    private Context context;
    private Map<String, String> postParams;

    public LandingRequestJson(Context context, JSONObject jsonReq, Response.Listener<WalLandingRespContainer> listener, Response.ErrorListener errorList) {
        super(Method.POST, context.getString(R.string.walgreen_api_url), jsonReq.toString(), listener, errorList);

        //Store refs to the listeners and context
        this.errList = errorList;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public Map<String, String> getParams() {
        return postParams;
    }

    @Override
    protected Response<WalLandingRespContainer> parseNetworkResponse(NetworkResponse response) {

        try {
            //Convert byte data into string
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject json = new JSONObject(jsonString);

            //Get the needed fields from the response
            String landURL = json.getString("landingUrl");
            String temp = json.getString("template");
            String upLim = json.getString("uploadLimit");
            String token = json.getString("token");

            //Build the container and return it as response generic with WalLandingRespContainer
            WalLandingRespContainer cont = new WalLandingRespContainer(landURL, temp, upLim, token);
            Response<WalLandingRespContainer> resp =
                    Response.success(cont,null);

            return resp;

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Error
        return null;
    }

    @Override
    protected void deliverResponse(WalLandingRespContainer response) {
        //Call the listener's handler
        listener.onResponse(response);
    }

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }
}
