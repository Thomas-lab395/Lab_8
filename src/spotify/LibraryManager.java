/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package spotify;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class LibraryManager {

    private final RandomAccessFile libraryFile;
    private static final String LIBRARY_DIR = "music_library";
    private static final String LIBRARY_FILE = "library.db";

    public LibraryManager() throws IOException {
        File dir = new File(LIBRARY_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.libraryFile = new RandomAccessFile(new File(dir, LIBRARY_FILE), "rw");
    }

 
    public void saveTrack(TrackRecord track) throws IOException {
        libraryFile.seek(libraryFile.length());
        libraryFile.writeInt(track.getId());
        libraryFile.writeUTF(track.getTitle());
        libraryFile.writeUTF(track.getArtist());
        libraryFile.writeDouble(track.getDurationInMinutes());
        libraryFile.writeUTF(track.getFilePath());
        libraryFile.writeUTF(track.getGenre());
        libraryFile.writeUTF(track.getArtworkPath());
        libraryFile.writeBoolean(false); 
    }

   
    public boolean markAsDeleted(int trackId) throws IOException {
        libraryFile.seek(0);
        while (libraryFile.getFilePointer() < libraryFile.length()) {
            long recordStartPosition = libraryFile.getFilePointer();
            int currentId = libraryFile.readInt();
            
            if (currentId == trackId) {
             
                libraryFile.seek(recordStartPosition);
                libraryFile.readInt(); 
                libraryFile.readUTF(); 
                libraryFile.readUTF(); 
                libraryFile.readDouble(); 
                libraryFile.readUTF(); 
                libraryFile.readUTF(); 
                libraryFile.readUTF(); 
                libraryFile.writeBoolean(true);
                return true;
            } else {
                
                libraryFile.readUTF();
                libraryFile.readUTF();
                libraryFile.readDouble();
                libraryFile.readUTF();
                libraryFile.readUTF();
                libraryFile.readUTF();
                libraryFile.readBoolean();
            }
        }
        return false;
    }

    public boolean trackExists(int trackId) throws IOException {
        libraryFile.seek(0);
        while (libraryFile.getFilePointer() < libraryFile.length()) {
            int id = libraryFile.readInt();
            libraryFile.readUTF();
            libraryFile.readUTF();
            libraryFile.readDouble();
            libraryFile.readUTF();
            libraryFile.readUTF();
            libraryFile.readUTF();
            boolean isDeleted = libraryFile.readBoolean();

            if (id == trackId && !isDeleted) {
                return true;
            }
        }
        return false;
    }

    public List<TrackRecord> loadAllTracks() throws IOException {
        List<TrackRecord> tracks = new ArrayList<>();
        libraryFile.seek(0);
        while (libraryFile.getFilePointer() < libraryFile.length()) {
            int id = libraryFile.readInt();
            String title = libraryFile.readUTF();
            String artist = libraryFile.readUTF();
            double duration = libraryFile.readDouble();
            String path = libraryFile.readUTF();
            String genre = libraryFile.readUTF();
            String artwork = libraryFile.readUTF();
            boolean isDeleted = libraryFile.readBoolean();

            if (!isDeleted) {
                tracks.add(new TrackRecord(id, title, artist, duration, path, genre, artwork));
            }
        }
        return tracks;
    }
}
