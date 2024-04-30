package com.mineclay.tclite.ui;

public class GUIResponse {

    public final static GUIResponse NOTHING = new GUIResponse(Action.DO_NOTHING, null);
    public final static GUIResponse CLOSE = new GUIResponse(Action.CLOSE, null);
    public final static GUIResponse REFRESH_BUTTON = new GUIResponse(Action.REFRESH_BUTTON, null);
    public final static GUIResponse REFRESH_GUI = new GUIResponse(Action.REFRESH_GUI, null);

    private final Action action;
    private final AbstractGUI gui;

    public GUIResponse(Action action, AbstractGUI gui) {
        this.action = action;
        this.gui = gui;
    }

    public Action getAction() {
        return action;
    }

    public AbstractGUI getGUI() {
        return gui;
    }

    public enum Action {
        DO_NOTHING, CLOSE, REFRESH_BUTTON, REFRESH_GUI, OPEN
    }

}
