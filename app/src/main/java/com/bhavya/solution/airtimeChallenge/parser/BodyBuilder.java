package com.bhavya.solution.airtimeChallenge.parser;

import android.text.TextUtils;

import com.bhavya.solution.airtimeChallenge.model.Command;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by bhavya.narra on 11/6/2016.
 */
public class BodyBuilder {

    public static final String MESSAGE = "message";
    public static final String COMMAND = "command";
    public static final String command_id = "bhavya_command";
    public static final String WRITINGS = "writing";
    public static final String ORDER = "order";
    public static final String POST= "report";


    /**
     * crates json body that needs to be sent while POST
     * @param c has command and roomId
     * @return
     * @throws JSONException
     */
    public JSONObject createJsonBody(Command c) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject commandJson = new JSONObject();
       if(TextUtils.equals(c.getCommand(), Command.EXPLORE)) {
           commandJson.put("explore", c.getRoomId());
        }
        else if(TextUtils.equals(c.getCommand(), Command.READ)) {
           commandJson.put("read", c.getRoomId());
       }
        jsonObject.put(command_id,commandJson);
        return jsonObject;
    }


    /**{
        "<commandId>": {
        "explore": "roomId"
    },
        "<commandId>": {
        "read": "roomId"
    }
    }**/
    //TODO verify implementtion
    public JSONObject createJSONBodyArray(ArrayList<Command> commands) throws JSONException {
        JSONObject   outer = new JSONObject();
        if(commands == null || commands.size() <1) {
            return outer;
        }
        for(int i=0; i<commands.size(); i++) {
            JSONObject inner = new JSONObject();
            Command c = commands.get(i);
            inner.put(c.getCommand(), c.getRoomId());
            outer.put(COMMAND+i, inner);
        }
        return outer;
    }

    /**
     * creates JSON body for final MESSAGE POST
     * @param command
     * @param message
     * @return
     * @throws JSONException
     */
    public JSONObject createResultJson(String command, String message) throws JSONException {
        JSONObject result = new JSONObject();
        if (TextUtils.equals(POST, command)) {
            result.put(MESSAGE, message);
        }
        return result;
    }
}