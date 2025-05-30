package com.example.solar_system;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class SpaceShipSound {

    private final MediaPlayer mediaPlayer;
    private final Media sound;
    private Node soundNode;

    /**
     * Creates a new SpaceShipSound with the given sound file path.
     * The sound will loop indefinitely and start silent.
     *
     * @param soundPath path to the sound file resource
     * @throws IllegalArgumentException if the sound file is not found
     */
    public SpaceShipSound(String soundPath) {
        URL resource = getClass().getResource(soundPath);
        if (resource == null) {
            throw new IllegalArgumentException("Sound file not found: " + soundPath);
        }

        sound = new Media(resource.toExternalForm());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(0.0); // Start silent until camera is close
    }

    /**
     * Attach this sound to a node in the scene.
     * Starts playing the sound immediately.
     *
     * @param soundNode the node to attach the sound to
     */
    public void attachToNode(Node soundNode) {
        this.soundNode = soundNode;
        mediaPlayer.play();
    }

    /**
     * Updates the volume based on the distance between the camera and the sound node.
     * Volume decreases linearly from full volume at min distance to zero at max distance.
     *
     * @param cameraGroup the group representing the camera position
     */
    public void updateVolumeRelativeToCamera(Group cameraGroup) {
        if (soundNode == null) return;

        double dx = cameraGroup.getTranslateX() - soundNode.getTranslateX();
        double dy = cameraGroup.getTranslateY() - soundNode.getTranslateY();
        double dz = cameraGroup.getTranslateZ() - soundNode.getTranslateZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double maxHearingDistance = 735;  // maximum range (can't hear audio beyond it)
        double minDistance = 100;          // minimum range (can hear audio until reaching maximum.)

        double volume;
        if (distance < minDistance) {
            volume = 1.0;
        } else if (distance > maxHearingDistance) {
            volume = 0.0;
        } else {
            volume = 1.0 - (distance - minDistance) / (maxHearingDistance - minDistance);
        }

        mediaPlayer.setVolume(volume * 0.6);
    }
}
