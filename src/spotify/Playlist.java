package spotify;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private PlaylistNode head;
    private PlaylistNode tail;
    private int count;

    public Playlist() {
        this.head = null;
        this.tail = null;
        this.count = 0;
    }

    public void addTrack(TrackRecord track) {
        PlaylistNode newNode = new PlaylistNode(track);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        count++;
    }

    public boolean removeTrackById(int trackId) {
        PlaylistNode current = head;
        PlaylistNode previous = null;

        while (current != null && current.track.getId() != trackId) {
            previous = current;
            current = current.next;
        }

        if (current == null) {
            return false;
        }

        if (previous == null) {
            head = current.next;
        } else {
            previous.next = current.next;
        }

        if (current == tail) {
            tail = previous; 
        }

        count--;
        return true;
    }

   
    public TrackRecord findTrackById(int trackId) {
        PlaylistNode current = head;
        while (current != null) {
            if (current.track.getId() == trackId) {
                return current.track;
            }
            current = current.next;
        }
        return null;
    }

   
    public List<TrackRecord> getAllTracks() {
        List<TrackRecord> trackList = new ArrayList<>();
        PlaylistNode current = head;
        while (current != null) {
            trackList.add(current.track);
            current = current.next;
        }
        return trackList;
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return head == null;
    }
}