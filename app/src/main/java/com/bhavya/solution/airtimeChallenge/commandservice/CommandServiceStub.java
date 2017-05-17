package com.bhavya.solution.airtimeChallenge.commandservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.bhavya.solution.airtimeChallenge.parser.BodyBuilder;
import com.bhavya.solution.airtimeChallenge.model.Command;
import com.bhavya.solution.airtimeChallenge.parser.DronesParser;
import com.bhavya.solution.airtimeChallenge.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by bhavya.narra on 11/6/2016.
 *
 */
public class CommandServiceStub {

    private static final String TAG = "AirTime-Stub";

    private static final String connection_url = "http://challenge2.airtime.com:10001";
    private static final String START = "/start";
    private static final String COMMAND = "/commands";
    private static final String DRONE = "/drone/";
    private static final String POST = "/report";
    private static final String permission = "com.bhavya.solution.labyrinthsolution.RECEIVE_POST";
    private StringBuilder POST_RESULT = new StringBuilder();
    private HashSet<String> mVisited = new HashSet();

    private ArrayList<String> mDrone_ids = new ArrayList();
    private  static int mSelectedDrone = 0;
    private static URL url = null;
    private static HttpURLConnection mConn = null;
    private static DronesParser mParser = null;
    private Context mContext;
    private static ConnectivityManager mConnectivityManager;
    private BodyBuilder mBodyBuilder;

    public CommandServiceStub(Context c) {
        mContext = c;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mParser = new DronesParser();
        mBodyBuilder = new BodyBuilder();
    }

    /**
     *
     * @throws IOException
     * @throws JSONException
     * gets all droneIDs and starts navigation based on available drone
     */
    public void getDrones() throws IOException, JSONException {
        String url = connection_url + START;
        connectToServer(url, "GET");
        if (mConn != null) {
            mConn.setRequestMethod("GET");
            mConn.connect();
            InputStream in = mConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String readLine = "";
            StringBuffer sb = new StringBuffer();
            while ((readLine = reader.readLine()) != null) {
                sb.append(readLine);
                Log.d(TAG, "output is: " + sb.toString());
            }
            reader.close();
            String room_id = mParser.getRootRoomID(sb.toString());
            mDrone_ids = mParser.parseDrones(sb.toString());
            findAvailableDrone();
            navigate(room_id);
        }
    }

    /**
     *
     * @param room_id is the root returned by DRONES REST-API interface
     * @throws JSONException
     * @throws IOException
     */
    public void navigate(String room_id) throws JSONException, IOException {
        Stack<String> stack = new Stack();
        stack.push(room_id);
        while (!stack.isEmpty()) {
            String current_roomID = stack.pop();
            mVisited.add(current_roomID);
            ArrayList<String> adjacentRooms = getAdjacentRooms(current_roomID);
            for (int i = 0; i < adjacentRooms.size(); i++) {
                if (!mVisited.contains(adjacentRooms.get(i)))
                    stack.push(adjacentRooms.get(i));
            }
        }
        readAllMessagesAndPostResult();
    }

    //TODO VERIFY CHANGED IMPLAMENTATION
    public void navigateModified(ArrayList<String> rooms)throws JSONException, IOException {
        Stack<ArrayList<String>> stack = new Stack<ArrayList<String>>();
        ArrayList<String> temp = new ArrayList();
        stack.push(rooms);
        while (!stack.isEmpty()) {
            ArrayList<String> currentList = stack.pop();
            mVisited.addAll(currentList);
            ArrayList<String> adjacentRooms = getAdjacentRooms(currentList);
            for (int i = 0; i < adjacentRooms.size(); i++) {
                if (!mVisited.contains(adjacentRooms.get(i))) {
                    temp.add(adjacentRooms.get(i));
                }
            }
            stack.push(temp);
        }
        readAllMessagesAndPostResult();
    }

    //TODO verify implementation
    public ArrayList<String> getAdjacentRooms(ArrayList<String> rooms) throws JSONException, IOException {
        ArrayList<String> adjacent = new ArrayList();
        if (rooms == null || rooms.size() < 1) {
            return adjacent;
        }
        int length = rooms.size(), i = 0;
        while (length > 5 && i < (length / 5) * 5) {
            ArrayList<Command> commands = new ArrayList();
            for (int j = 0; j < 5; j++) {
                Command c = new Command();
                c.setCommand(Command.EXPLORE);
                c.setRoomId(rooms.get(i));
                commands.add(c);
                i++;
            }
            JSONObject jBody = mBodyBuilder.createJSONBodyArray(commands);
            String url = connection_url + DRONE + mDrone_ids.get(3) + COMMAND;
            connectToServer(url, "POST");
            if (mConn != null) {
                OutputStreamWriter wr = new OutputStreamWriter(mConn.getOutputStream());
                wr.write(jBody.toString());
                wr.flush();
                mConn.setRequestMethod("POST");
                mConn.connect(); StringBuilder sb = new StringBuilder();
                if(mConn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    findAvailableDrone();
                }
                if(mConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    mConn.disconnect();
                    Log.d(TAG, "getAdjRooms: " + sb.toString());
                    adjacent = mParser.parseConnectionsList(sb.toString());
                }
            }
            i++;
        }
            while (i < length) {
                //TO-DO implement same above logic
                i++;
            }
            return adjacent;
        }
    /**
     * Reads messages of all the rooms in the labyrinth
     * and sorts them according to their respective order
     * @throws JSONException
     * @throws IOException
     */
    public void readAllMessagesAndPostResult() throws JSONException, IOException{
        List<String> list = new ArrayList<String>(mVisited);
        Map<Integer, String> map = new HashMap<Integer, String>();
        for (int i = 0; i < list.size(); i++) {
            Message msg = readMessage(list.get(i));
            if(!TextUtils.equals("-1", msg.getOrder())) {
                map.put(Integer.valueOf(msg.getOrder()), msg.getMessage());
            }
        }
        for (HashMap.Entry<Integer, String> entry : map.entrySet()) {
            Log.d(TAG, "msg is: " + entry.getValue()+ "order is: "+ entry.getKey());
        }
        Object[] a = map.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<Integer, String>) o1).getKey()
                        .compareTo(((Map.Entry<Integer, String>) o2).getKey());
            }
        });
        for (Object e : a) {
            POST_RESULT.append(((Map.Entry<Integer, String>)e).getValue());
            postResult(POST_RESULT.toString());
        }
    }

    /**
     *
      * @param result - concatenated Message result
     * @throws IOException
     * @throws JSONException
     * posts @param result to the server
     */
    private void postResult(String result) throws IOException, JSONException{
        JSONObject jBody = mBodyBuilder.createResultJson(BodyBuilder.POST, result);
        String url = connection_url + POST;
        connectToServer(url, "POST");
        if (mConn != null) {
            OutputStreamWriter wr = new OutputStreamWriter(mConn.getOutputStream());
            wr.write(jBody.toString());
            wr.flush();
            mConn.setRequestMethod("POST");
            mConn.connect();
            StringBuilder sb = new StringBuilder();
            if(mConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                mConn.disconnect();
                Intent i = new Intent("com.intent.airtime.CHALLENGE_COMPLETED_ACTION");
                mContext.sendBroadcast(i, permission);
            }

            Log.d(TAG, "POSTED RESULT" + sb.toString());
        }
    }

    /**
     *
     * gets list of all the rooms connected to current roomID
     * @param room_id - current room
     * @return list of rooms connected to current room
     * @throws IOException
     * @throws JSONException
     *
     */
    public ArrayList<String> getAdjacentRooms(String room_id) throws IOException, JSONException {
        ArrayList<String> adj = new ArrayList();
        findAvailableDrone();
        Command c = new Command();
        c.setCommand(Command.EXPLORE);
        c.setRoomId(room_id);
        JSONObject jBody = mBodyBuilder.createJsonBody(c);
        String url = connection_url + DRONE + mDrone_ids.get(3) + COMMAND;
        connectToServer(url, "POST");
        if (mConn != null) {
            OutputStreamWriter wr = new OutputStreamWriter(mConn.getOutputStream());
            wr.write(jBody.toString());
            wr.flush();
            mConn.setRequestMethod("POST");
            mConn.connect();
            StringBuilder sb = new StringBuilder();
            if(mConn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                findAvailableDrone();
            }
            if(mConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                mConn.disconnect();
                Log.d(TAG, "getAdjRooms: " + sb.toString());
                adj = mParser.parseConnections(sb.toString());
            }
        }
        return adj;
    }

    /**
     *
     *getsWriting of a room
     * @param roomId
     * @return Message<message, order><
     * @throws IOException
     * @throws JSONException
     */
    //FIXME COI to batchup readCommands
    private Message readMessage(String roomId) throws IOException, JSONException {
        findAvailableDrone();
        Message msg = null;
        Command c = new Command();
        c.setCommand(Command.READ);
        c.setRoomId(roomId);
        JSONObject jBody = mBodyBuilder.createJsonBody(c);
        String url = connection_url + DRONE + mDrone_ids.get(mSelectedDrone) + COMMAND;
        connectToServer(url, "POST");
        if (mConn != null) {
            OutputStreamWriter wr = new OutputStreamWriter(mConn.getOutputStream());
            wr.write(jBody.toString());
            wr.flush();
            mConn.setRequestMethod("POST");
            mConn.connect();
            StringBuilder sb = new StringBuilder();
            if (mConn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                findAvailableDrone();
            }
            if (mConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mConn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                mConn.disconnect();
                Log.d(TAG, "getMessage: " + sb.toString());
                msg = mParser.parseWritings(sb.toString());
            }
        }
        return msg;
    }

    /**
     * finds available drone
     * If no one is available
     * kills the service
     * @throws IOException
     */
    private void findAvailableDrone() throws IOException, JSONException {
        int resCode;
        mSelectedDrone = 0;
        for (int i = 0; i < mDrone_ids.size(); i++) {
            String url = connection_url + DRONE + mDrone_ids.get(mSelectedDrone) + COMMAND;
            connectToServer(url, "POST");
            if (mConn != null) {
                Command c = new Command();
                c.setCommand(Command.READ);
                c.setRoomId("ea01a3363429596a0cefd0de0bb1df15");
                JSONObject jBody = mBodyBuilder.createJsonBody(c);
                OutputStreamWriter wr = new OutputStreamWriter(mConn.getOutputStream());
                wr.write(jBody.toString());
                wr.flush();
                mConn.setRequestMethod("POST");
                mConn.connect();
                resCode = mConn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "mSelectedDrone: " + mDrone_ids.get(mSelectedDrone));
                    mConn.disconnect();
                    return;
                }
                mSelectedDrone++;
            }
        }
        mSelectedDrone = -1;
        Log.d(TAG, "There are no drones available currently");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     *
     * Connects to a specific server url passed
     * @param conn_url
     * @param command
     * @throws IOException
     */
    private void connectToServer(String conn_url, String command) throws IOException {
        if (!isNetworkConnected()) {
            return;
        }
        url = new URL(conn_url);
        mConn = (HttpURLConnection) url.openConnection();
        if (mConn != null) {
            mConn.setRequestProperty("x-commander-email", "bhavya1510@gmail.com");
            if(command.equals("POST")) {
                mConn.setDoOutput(true);
                mConn.setDoInput(true);
                mConn.setRequestProperty("Content-Type", "application/json");
            }
        }
    }

    /**
     * verifies if device is connected to network
     * @return boolean
     * @throws IOException
     */
    private boolean isNetworkConnected() throws IOException {

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            Log.v(TAG, "networkInfo is null");
            return false;
        }
        return networkInfo.isConnected();
    }


}
