package edu.umkc.rupee.lib;

import java.util.logging.Level;

import org.postgresql.util.PGobject;

public class Log extends PGobject {

    private Level level;
    private String exception;
    private String message;

    public Log (Level level, String exception, String message) {
       
        this.level = level; 
        this.exception = exception;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getValue() {
        String row = "(" 
            + level.toString() + "," + exception + "," + message
            + ")";
        return row;
    }
}
