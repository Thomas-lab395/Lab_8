package spotify;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private LibraryManager libraryManager;
    private PlaybackEngine playbackEngine;
    private Playlist playlist;

    private TrackRecord selectedTrack;
    private TrackRecord currentTrack;
    
    private JLabel artworkLabel, trackInfoLabel, timeElapsedLabel, timeRemainingLabel;
    private JProgressBar progressBar;
    private Timer progressTimer;
    private int elapsedSeconds = 0;
    
    private JTable playlistTable;
    private DefaultTableModel tableModel;
    private JButton pauseButton;

    public MainWindow() {
        super("Music Player");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        initializeCore();
        setupUI();
        loadPlaylistIntoTable();
    }
    
    private void initializeCore() {
        try {
            libraryManager = new LibraryManager();
            playbackEngine = new PlaybackEngine();
            playlist = new Playlist();
            
            for (TrackRecord track : libraryManager.loadAllTracks()) {
                playlist.addTrack(track);
            }
        } catch (IOException e) {
            showError("Error al inicializar la librería de música: " + e.getMessage());
        }
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(30, 30, 30));
        setContentPane(mainPanel);

        JLabel titleLabel = UIFactory.createLabel("Music Player", 0, 10, 600, 40, Font.BOLD, 30f);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(titleLabel);

        String[] columnNames = {"ID", "Título", "Artista"};
        tableModel = new DefaultTableModel(columnNames, 0);
        playlistTable = new JTable(tableModel);
        playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistTable.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        scrollPane.setBounds(50, 60, 500, 200);
        mainPanel.add(scrollPane);

        artworkLabel = new JLabel("Selecciona una canción", JLabel.CENTER);
        artworkLabel.setBounds(200, 270, 200, 200);
        artworkLabel.setForeground(Color.LIGHT_GRAY);
        artworkLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(artworkLabel);

        trackInfoLabel = UIFactory.createLabel("...", 0, 480, 600, 30, Font.BOLD, 18f);
        trackInfoLabel.setForeground(Color.WHITE);
        trackInfoLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(trackInfoLabel);

        timeElapsedLabel = UIFactory.createLabel("00:00", 50, 520, 50, 20, Font.PLAIN, 12f);
        timeElapsedLabel.setForeground(Color.WHITE);
        mainPanel.add(timeElapsedLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(110, 520, 380, 15);
        progressBar.setValue(0);
        mainPanel.add(progressBar);

        timeRemainingLabel = UIFactory.createLabel("-00:00", 500, 520, 50, 20, Font.PLAIN, 12f);
        timeRemainingLabel.setForeground(Color.WHITE);
        mainPanel.add(timeRemainingLabel);

        JButton playButton = UIFactory.createButton("Play", 250, 560, 100, 35);
        pauseButton = UIFactory.createButton("Pause", 140, 560, 100, 35);
        JButton stopButton = UIFactory.createButton("Stop", 360, 560, 100, 35);
        JButton addButton = UIFactory.createButton("Add Song", 140, 610, 100, 35);
        JButton removeButton = UIFactory.createButton("Remove Song", 360, 610, 120, 35);

        mainPanel.add(playButton);
        mainPanel.add(pauseButton);
        mainPanel.add(stopButton);
        mainPanel.add(addButton);
        mainPanel.add(removeButton);

        playButton.addActionListener(e -> onPlay());
        pauseButton.addActionListener(e -> onTogglePauseResume());
        stopButton.addActionListener(e -> onStop());
        addButton.addActionListener(e -> onAddTrack());
        removeButton.addActionListener(e -> onRemoveTrack());
        
        playlistTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && playlistTable.getSelectedRow() != -1) {
                int selectedRow = playlistTable.getSelectedRow();
                int trackId = (int) tableModel.getValueAt(selectedRow, 0);
                
                TrackRecord foundTrack = playlist.findTrackById(trackId);
                if (foundTrack != null) {
                    onStop();
                    selectedTrack = foundTrack;
                    updateUI(selectedTrack);
                }
            }
        });

        updateUI(null);
    }
    
    private void loadPlaylistIntoTable() {
        tableModel.setRowCount(0);
        for (TrackRecord track : playlist.getAllTracks()) {
            tableModel.addRow(new Object[]{track.getId(), track.getTitle(), track.getArtist()});
        }
    }
    
    private void onPlay() {
        if (selectedTrack == null) {
            showMessage("Por favor, seleccione una canción de la tabla.");
            return;
        }

        if (playbackEngine.isPaused() && selectedTrack.equals(currentTrack)) {
            playbackEngine.play();
            startTimer();
            pauseButton.setText("Pause");
        } else {
            currentTrack = selectedTrack;
            if (!new File(currentTrack.getFilePath()).exists()) {
                showError("El archivo de audio no se encuentra en la ruta especificada.");
                return;
            }
            
            playbackEngine.load(currentTrack.getFilePath());
            resetTimer();
            playbackEngine.play();
            startTimer();
            updateUI(currentTrack);
            pauseButton.setText("Pause");
        }
    }
    
    private void onTogglePauseResume() {
        if (playbackEngine.isPlaying()) {
            playbackEngine.pause();
            stopTimer();
            pauseButton.setText("Resume");
        } else if (playbackEngine.isPaused()) {
            playbackEngine.play();
            startTimer();
            pauseButton.setText("Pause");
        }
    }
    
    private void onStop() {
        playbackEngine.stop();
        currentTrack = null;
        resetTimer();
        updateUI(selectedTrack);
        pauseButton.setText("Pause");
    }
    
    private void onAddTrack() {
        JFileChooser audioChooser = new JFileChooser();
        audioChooser.setDialogTitle("Seleccionar archivo MP3");
        audioChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));
        if (audioChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File audioFile = audioChooser.getSelectedFile();

        JFileChooser imageChooser = new JFileChooser();
        imageChooser.setDialogTitle("Seleccionar carátula (opcional)");
        imageChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
        String imagePath = (imageChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
            ? imageChooser.getSelectedFile().getAbsolutePath() 
            : "";

        try {
            String idStr = JOptionPane.showInputDialog(this, "ID numérico único para la canción:");
            if(idStr == null) return;
            int id = Integer.parseInt(idStr);

            if (playlist.findTrackById(id) != null || libraryManager.trackExists(id)) {
                showError("El ID " + id + " ya está en uso.");
                return;
            }
            
            String title = JOptionPane.showInputDialog(this, "Título:", audioFile.getName().replace(".mp3", ""));
            if(title == null) return;
            String artist = JOptionPane.showInputDialog(this, "Artista:", "Desconocido");
            if(artist == null) return;
            String genre = JOptionPane.showInputDialog(this, "Género:", "Desconocido");
            if(genre == null) return;

            int durationInSeconds = calculateDuration(audioFile);
            double durationInMinutes = durationInSeconds / 60.0;
            
            TrackRecord newTrack = new TrackRecord(id, title, artist, durationInMinutes, audioFile.getAbsolutePath(), genre, imagePath);
            
            libraryManager.saveTrack(newTrack);
            playlist.addTrack(newTrack);
            
            tableModel.addRow(new Object[]{newTrack.getId(), newTrack.getTitle(), newTrack.getArtist()});
            
            showMessage("Canción añadida exitosamente.");

        } catch (NumberFormatException e) {
            showError("El ID debe ser un número.");
        } catch (IOException e) {
            showError("Error al guardar la canción: " + e.getMessage());
        }
    }

    private void onRemoveTrack() {
        int selectedRow = playlistTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Por favor, selecciona una canción de la tabla para eliminar.");
            return;
        }
        
        int trackId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            if (currentTrack != null && currentTrack.getId() == trackId) {
                onStop();
            }
            
            libraryManager.markAsDeleted(trackId);
            playlist.removeTrackById(trackId);

            tableModel.removeRow(selectedRow);

            showMessage("Canción eliminada.");
            selectedTrack = null;
            updateUI(null);

        } catch (IOException e) {
            showError("Error al eliminar la canción: " + e.getMessage());
        }
    }

    private void updateUI(TrackRecord track) {
        if (track == null) {
            artworkLabel.setIcon(null);
            artworkLabel.setText("Selecciona una canción");
            trackInfoLabel.setText("...");
            timeElapsedLabel.setText("00:00");
            timeRemainingLabel.setText("-00:00");
            progressBar.setValue(0);
            return;
        }

        trackInfoLabel.setText(track.getTitle() + " - " + track.getArtist());
        
        File artworkFile = new File(track.getArtworkPath());
        if (artworkFile.exists() && !track.getArtworkPath().isBlank()) {
            ImageIcon icon = new ImageIcon(track.getArtworkPath());
            Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            artworkLabel.setIcon(new ImageIcon(scaledImage));
            artworkLabel.setText("");
        } else {
            artworkLabel.setIcon(null);
            artworkLabel.setText("No artwork");
        }
        
        int totalSeconds = (int)(track.getDurationInMinutes() * 60);
        timeRemainingLabel.setText("-" + formatTime(totalSeconds));
    }
    
    private void startTimer() {
        if (progressTimer != null && progressTimer.isRunning()) return;
        progressTimer = new Timer(1000, e -> {
            if (!playbackEngine.isPlaying() || currentTrack == null) {
                stopTimer();
                if (!playbackEngine.isPaused()) onStop();
                return;
            }
            elapsedSeconds++;
            int totalSeconds = (int) (currentTrack.getDurationInMinutes() * 60);
            if (elapsedSeconds > totalSeconds) elapsedSeconds = totalSeconds;
            int progress = (int) (((double) elapsedSeconds / totalSeconds) * 100.0);
            progressBar.setValue(progress);
            timeElapsedLabel.setText(formatTime(elapsedSeconds));
            timeRemainingLabel.setText("-" + formatTime(totalSeconds - elapsedSeconds));
            if (elapsedSeconds >= totalSeconds) onStop();
        });
        progressTimer.start();
    }

    private void stopTimer() {
        if (progressTimer != null) progressTimer.stop();
    }

    private void resetTimer() {
        stopTimer();
        this.elapsedSeconds = 0;
        progressBar.setValue(0);
        timeElapsedLabel.setText("00:00");
        if (selectedTrack != null) {
             int totalSeconds = (int)(selectedTrack.getDurationInMinutes() * 60);
             timeRemainingLabel.setText("-" + formatTime(totalSeconds));
        } else {
            timeRemainingLabel.setText("-00:00");
        }
    }
    
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private int calculateDuration(File mp3File) {
        try (FileInputStream fis = new FileInputStream(mp3File)) {
            Bitstream bitstream = new Bitstream(fis);
            Header header = bitstream.readFrame();
            if (header != null) return (int) header.total_ms((int) mp3File.length()) / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}