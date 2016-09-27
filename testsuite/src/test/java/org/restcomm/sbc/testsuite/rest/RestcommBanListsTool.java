package org.restcomm.sbc.testsuite.rest;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.BlackList;
import org.restcomm.sbc.bo.WhiteList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.thoughtworks.xstream.XStream;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    19 sept. 2016 13:43:58
 * @class   RestcommBanListsTool.java
 *
 */
public class RestcommBanListsTool {

    private static RestcommBanListsTool instance;
    private static String accountsUrl;
    private static Logger logger = Logger.getLogger(RestcommBanListsTool.class);
    
    private RestcommBanListsTool() {}

    public static RestcommBanListsTool getInstance() {
        if (instance == null)
            instance = new RestcommBanListsTool();

        return instance;
    }

    private String getAccountsUrl(BanList.Type color, String deploymentUrl, String username, Boolean json) {
        if (deploymentUrl.endsWith("/")) {
            deploymentUrl = deploymentUrl.substring(0, deploymentUrl.length() - 1);
        }
        

        accountsUrl = deploymentUrl + "/2012-04-24/Accounts/" + username + "/"+color+"List" + ((json) ? ".json" : "");

        return accountsUrl;
    }

    

    public JsonObject getBanLists(BanList.Type color, String deploymentUrl, String username, String authToken) {
        return (JsonObject) getBanLists(color, deploymentUrl, username, authToken, null, null, true);
    }

    public JsonObject getBanLists(BanList.Type color, String deploymentUrl, String username, String authToken, Integer page, Integer pageSize,
            Boolean json) {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(color, deploymentUrl, username, json);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        if (page != null || pageSize != null) {
            MultivaluedMap<String, String> params = new MultivaluedMapImpl();

            if (page != null)
                params.add("Page", String.valueOf(page));
            if (pageSize != null)
                params.add("PageSize", String.valueOf(pageSize));

            response = webResource.queryParams(params).accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                    .get(String.class);
        } else {
            response = webResource.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).get(String.class);
        }

        JsonParser parser = new JsonParser();

        if (json) {
            JsonObject jsonObject = null;
            try {
                JsonElement jsonElement = parser.parse(response);
                if (jsonElement.isJsonObject()) {
                    jsonObject = jsonElement.getAsJsonObject();
                } else {
                    logger.info("JsonElement: " + jsonElement.toString());
                }
            } catch (Exception e) {
                logger.info("Exception during JSON response parsing, exception: "+e);
                logger.info("JSON response: "+response);
            }
            return jsonObject;
        } else {
            XStream xstream = new XStream();
            
            switch(color) {
            	case WHITE:
            		xstream.alias("banlist", WhiteList.class);
            		break;
            	default:
            		xstream.alias("banlist", BlackList.class);
            }
           
            JsonObject jsonObject = parser.parse(xstream.toXML(response)).getAsJsonObject();
            return jsonObject;
        }

    }

    public JsonObject getBanList(BanList.Type color, String deploymentUrl, String username, String authToken, String sid){

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(color, deploymentUrl, username, false);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        webResource = webResource.path(String.valueOf(sid)+".json");
        logger.info("The URI to sent: "+webResource.getURI());
        
        response = webResource.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        return jsonObject;

    }

    public JsonObject getBanListUsingFilter(BanList.Type color,String deploymentUrl, String username, String authToken, Map<String, String> filters) {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(color, deploymentUrl, username, true);

        WebResource webResource = jerseyClient.resource(url);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        for (String filterName : filters.keySet()) {
            String filterData = filters.get(filterName);
            params.add(filterName, filterData);
        }
        webResource = webResource.queryParams(params);
        String response = webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        return jsonObject;
    }

    public JsonElement createBan(BanList.Type color,String deploymentUrl, String username, String authToken, String from, String to, String rcmlUrl) {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(color, deploymentUrl, username, true);

        WebResource webResource = jerseyClient.resource(url);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("From", from);
        params.add("To", to);
        params.add("Url", rcmlUrl);

        // webResource = webResource.queryParams(params);
        String response = webResource.accept(MediaType.APPLICATION_JSON).post(String.class, params);
        JsonParser parser = new JsonParser();
        if (response.startsWith("[")) {
            return parser.parse(response).getAsJsonArray();
        } else {
            return parser.parse(response).getAsJsonObject();
        }
    }

    public JsonObject modifyBan(BanList.Type color, String deploymentUrl, String username, String authToken, String callSid, String status,
            String rcmlUrl) throws Exception {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(color, deploymentUrl, username, true);

        WebResource webResource = jerseyClient.resource(url);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        if (status != null && rcmlUrl != null) {
            throw new Exception(
                    "You can either redirect a call using the \"url\" attribute or terminate it using the \"status\" attribute!");
        }
        if (status != null)
            params.add("Status", status);
        if (rcmlUrl != null)
            params.add("Url", rcmlUrl);

        JsonObject jsonObject = null;

        try {
            String response = webResource.path(callSid).accept(MediaType.APPLICATION_JSON).post(String.class, params);
            JsonParser parser = new JsonParser();
            jsonObject = parser.parse(response).getAsJsonObject();
        } catch (Exception e) {
            logger.info("Exception e: "+e);
            UniformInterfaceException exception = (UniformInterfaceException)e;
            jsonObject = new JsonObject();
            jsonObject.addProperty("Exception",exception.getResponse().getStatus());
        }
        return jsonObject;
    }

    
}
