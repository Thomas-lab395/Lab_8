package spotify;


public class TrackRecord {

    private final int id;
    private final String title;
    private final String artist;
    private final double durationInMinutes;
    private final String filePath;
    private final String genre;
    private final String artworkPath;

    public TrackRecord(int id, String title, String artist, double durationInMinutes, String filePath, String genre, String artworkPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.durationInMinutes = durationInMinutes;
        this.filePath = filePath;
        this.genre = genre;
        this.artworkPath = artworkPath;
    }

   
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public double getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getGenre() {
        return genre;
    }

    public String getArtworkPath() {
        return artworkPath;
    }
}