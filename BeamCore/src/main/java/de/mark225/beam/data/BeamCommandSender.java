package de.mark225.beam.data;

public interface BeamCommandSender {
    void sendMessage(String message);
    boolean checkPermission(String permission);

}
