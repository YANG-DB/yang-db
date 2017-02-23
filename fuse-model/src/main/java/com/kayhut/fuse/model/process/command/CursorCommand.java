package com.kayhut.fuse.model.process.command;

/**
 * Created by lior on 23/02/2017.
 */
public class CursorCommand {
    private String id;
    private String command;//todo enum [fetch,plan,delete]
    private String[] params;


    public CursorCommand(String id, String command, String... params) {
        this.id = id;
        this.command = command;
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String[] getParams() {
        return params;
    }
}
