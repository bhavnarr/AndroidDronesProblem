package com.bhavya.solution.airtimeChallenge.model;

/**
 * Created by bhavya.narra on 11/6/2016.
 */


public class Message {
    private String message;
    private String order;

    /**
     *
     * @param msg
     */
    public void setMessage(String msg) {
        this.message = msg;
    }

    /**
     *
     * @param order
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     *
     * @return message
     */
    public String getMessage() {

        return this.message;
    }

    /**
     *
     * @return order
     */
    public String getOrder() {
        return this.order;
    }
}
