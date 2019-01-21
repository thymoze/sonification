package mupro.hcm.sonification.utils;

public enum Direction {
    UP("up"),
    DOWN("down");

    private String id;

    Direction(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
