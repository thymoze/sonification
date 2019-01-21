package mupro.hcm.sonification.sound;

public class Sound {
    private String instrument;
    private Direction direction;

    public Sound(String instrument, Direction direction) {
        this.instrument = instrument;
        this.direction = direction;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}