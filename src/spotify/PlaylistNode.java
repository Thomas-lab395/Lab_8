package spotify;

public class PlaylistNode {

    public TrackRecord track;
    public PlaylistNode next;

    public PlaylistNode(TrackRecord track) {
        this.track = track;
        this.next = null;
    }
}