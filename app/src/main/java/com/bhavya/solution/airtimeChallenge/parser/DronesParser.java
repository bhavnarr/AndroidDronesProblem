package com.bhavya.solution.airtimeChallenge.parser;

import android.util.Log;

import com.bhavya.solution.airtimeChallenge.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * parses JSON responses from REST API
 */
public class DronesParser {
    private static final String TAG = "AirTime-Parser";
    private static  JSONObject jsonObject;
    private static final String  roomID = "roomId";
    private static final String DRONES = "drones";
    private static final String EMPTY = "";
    private static final String CONNECTIONS = "connections";

    /**
     *
     * @param data
     * @return the roomID
     * @throws JSONException
     */
    public String getRootRoomID (String data) throws JSONException {
        if(data == null) {
          return EMPTY;
        }
        String room_id;
        jsonObject = new JSONObject(data);
        room_id = jsonObject.getString(roomID);
        Log.v(TAG, "room_id: "+ room_id);
        return room_id;
    }

    /**
     *
     * @param data
     * @return Drones
     * @throws JSONException
     */
    public ArrayList<String> parseDrones (String data) throws JSONException {
        ArrayList<String> drones = new ArrayList();
        if(data == null) {
            return drones;
        }
        jsonObject = new JSONObject(data);
        String droneString =jsonObject.getString(DRONES);
            Log.v(TAG, " drones: "+ droneString);
        drones = convertStringToArray(droneString);
        return drones;
    }


    /**
     *
     * @param data
     * @return connected rooms
     * @throws JSONException
     */
    public ArrayList<String> parseConnections (String data) throws  JSONException {
        ArrayList<String> adjRooms = new ArrayList();
        if(data == null) {
            Log.v(TAG, "data is null");
            return adjRooms;
        }
        JSONObject jObject = new JSONObject(data);
        JSONObject command_id = jObject.getJSONObject(BodyBuilder.command_id);
        String connections = command_id.getString(CONNECTIONS);
        adjRooms = convertStringToArray(connections);
        return adjRooms;
    }

    /**
     *
     * @param data
     * @return writings on a room
     * @throws JSONException
     */
    public Message parseWritings(String data)throws JSONException {
        Message res =  null;
        if(data == null) {
            Log.v(TAG, "ParseWritings: data is null");
            return res;
        }
        JSONObject jsonObject = new JSONObject(data);
        JSONObject command_id = jsonObject.getJSONObject(BodyBuilder.command_id);
        String msg = command_id.getString(BodyBuilder.WRITINGS);
        String order = command_id.getString(BodyBuilder.ORDER);
        res = new Message();
        res.setMessage(msg);
        res.setOrder(order);
        return res;

    }

    //FIXME COI batchUpCommands for READ
    public ArrayList<String> parseConnectionsList(String str) throws JSONException{
        ArrayList<String> connecting_rooms = new ArrayList();
        JSONObject jObject = new JSONObject(str);
        Iterator<String> keys = jObject.keys();
        int i=0;
        while(keys.hasNext()) {
            if(jObject.has(BodyBuilder.COMMAND+i)) {
                JSONObject obj = jObject.getJSONObject("connection"+i);
                if(obj.has(CONNECTIONS)) {
                    String connections = obj.getString(CONNECTIONS);
                    ArrayList<String> conns = convertStringToArray(connections);
                    connecting_rooms.addAll(conns);
                } else if(obj.has("WRITINGS")) {
                    //String writing = obj.
                }
            }
            i++;
        }
        return connecting_rooms;
    }

    /**
     *
     * @param str
     * @return array of objects
     */
    private ArrayList<String> convertStringToArray(String str) {
        ArrayList<String> res = new ArrayList();
        str = str.replace("[", "");
        str = str.replace("]", "");
        str = str.replace("\"", "");
        StringTokenizer st = new StringTokenizer(str, ",");
        while(st.hasMoreElements()) {
            res.add((String )st.nextElement());
        }
        return res;
    }

}
