package com.bhavya.solution.airtimeChallenge.model;


public class Command {
    public static final String READ = "read";
    public static final String EXPLORE = "explore";
    private String command;
    private String roomId;


    /**
     *
     * @param command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     *
     * @param roomID
     */
    public void setRoomId(String roomID) {
        this.roomId = roomID;
    }

    /**
     *
     * @return the ID
     */
    public String getCommand() {

        return this.command;
    }

    /**
     *
     * @return the roomID
     */
    public String getRoomId(){
        return this.roomId;
    }
}
