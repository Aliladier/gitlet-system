package gitlet;

// TODO: any imports you need here

import java.io.*;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String id;
    private String parent;
    private String commitTime;
    private HashMap<String, String> contents;

    public String getTime(){
        SimpleDateFormat myFmt3 = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        Date now = new Date();
        return myFmt3.format(now);
    }

    public Commit(String id, String message, String parent, HashMap<String,String > contents){
        this.id = id;
        this.message = message;
        this.parent = parent;
        this.commitTime = getTime();
        this.contents = contents;
    }

    public Commit(String message, String parent, HashMap<String,String> contents){
        this.message = message;
        this.parent = parent;
        this.contents = contents;
        this.commitTime = getTime();
        this.id = computehash();
    }

    public String computehash(){
        byte[] commitObj = Utils.serialize(this);
        return Utils.sha1(commitObj);
    }

    public String getMessage() {
        return message;
    }

    public String getId(){
        return id;
    }

    public String getParent(){
        return parent;
    }

    public String getCommitTime(){
        return commitTime;
    }

    public HashMap<String, String> getContents(){
        return this.contents;
    }

    @Override
    public String toString(){
        return ("===\n" +
                "commit " + id + "\n" +
                "Date: " + commitTime + "\n"+
                message + "\n");
    }


    /* TODO: fill in the rest of this class. */
}
