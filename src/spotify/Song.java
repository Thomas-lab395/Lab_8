package spotify;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.io.File;

public class Song {
    
    String filePath;
    String title;
    String artist;
    String genre;
    int duration; 
    
    Song next;

    public Song(String filePath) {
        this.filePath = filePath;
        this.next = null;
        loadMetadata();
    }

    private void loadMetadata() {
        try {
            File file = new File(this.filePath);
            this.title = file.getName().replaceFirst("[.][^.]+$", "");

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            this.duration = audioFile.getAudioHeader().getTrackLength();

            this.artist = (tag != null && tag.getFirst(FieldKey.ARTIST) != null) ? tag.getFirst(FieldKey.ARTIST) : "Artista Desconocido";
            this.genre = (tag != null && tag.getFirst(FieldKey.GENRE) != null) ? tag.getFirst(FieldKey.GENRE) : "Desconocido";

        } catch (Exception e) {
            System.err.println("Error al leer metadatos para " + this.filePath + ": " + e.getMessage());
            this.artist = "Artista Desconocido";
            this.genre = "Desconocido";
            this.duration = 0;
        }
    }

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
   
    @Override
    public String toString() {
        return this.title + " - " + this.artist;
    }
}