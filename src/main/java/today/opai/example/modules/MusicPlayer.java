package today.opai.example.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import today.opai.api.enums.EnumModuleCategory;
import today.opai.api.features.ExtensionModule;
import today.opai.api.interfaces.EventHandler;
import today.opai.example.ExampleExtension;

public class MusicPlayer extends ExtensionModule implements EventHandler {
    public MusicPlayer() {
        super("Music Player", "YouTube music player using yt-dlp", EnumModuleCategory.MISC);
        INSTANCE = this;
    }
    // Pause playback
    public void pause() {
        isPaused = true;
        status = "Paused";
    }

    // Resume playback
    public void resume() {
        isPaused = false;
        status = "Playing";
    }

    // Add to playlist
    public void addToPlaylist(String url) {
        playlist.add(url);
        ExampleExtension.openAPI.printMessage("§aAdded to playlist: " + url);
    }

    // Clear playlist
    public void clearPlaylist() {
        playlist.clear();
        currentIndex = 0;
        ExampleExtension.openAPI.printMessage("§aPlaylist cleared");
    }

    // Stop playback method
    public void stop() {
        isPlaying = false;
        isPaused = false;
        currentSong = "";
        currentArtist = "";
        status = "Stopped";
    }
    public static MusicPlayer INSTANCE;
    // Settings
    private boolean showWidget = true;
    private int volume = 50;
    private boolean autoPlay = false;
    // Player state
    private Process currentProcess;
    private String currentSong = "";
    private String currentArtist = "";
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private List<String> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private String status = "Idle";

    public void play(String url) {
        if (isPlaying) {
            stop();
        }
        CompletableFuture.runAsync(() -> {
            try {
                status = "Loading...";
                // Get video info (optional, for display)
                String ytDlpPath = System.getenv("APPDATA") + "\\.minecraft\\yt-dlp.exe";
                ProcessBuilder infoBuilder = new ProcessBuilder(
                    ytDlpPath, "--print", "%(title)s\n%(uploader)s", url
                );
                Process infoProcess = infoBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(infoProcess.getInputStream()));
                String title = reader.readLine();
                String uploader = reader.readLine();
                reader.close();
                int infoExit = infoProcess.waitFor();
                if (title != null && uploader != null) {
                    currentSong = title;
                    currentArtist = uploader;
                } else {
                    currentSong = "Unknown";
                    currentArtist = "Unknown";
                }
                if (infoExit != 0) {
                    ExampleExtension.openAPI.printMessage("§c[yt-dlp error] Could not fetch video info");
                }
                // Download audio to a temp file with .mp3 extension
                File tempFile = File.createTempFile("ytmusic", ".mp3");
                String tempFilePath = tempFile.getAbsolutePath();
                // Always delete the temp file if it exists (yt-dlp will not overwrite by default)
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) {
                        ExampleExtension.openAPI.printMessage("§c[yt-dlp error] Could not delete old temp file: " + tempFilePath);
                        return;
                    }
                    // Re-create a new temp file
                    tempFile = new File(tempFilePath);
                }
                ExampleExtension.openAPI.printMessage("§e[debug] yt-dlp path: " + ytDlpPath);
                ExampleExtension.openAPI.printMessage("§e[debug] tempFilePath: " + tempFilePath);
                ProcessBuilder ytDlpPb = new ProcessBuilder(
                    ytDlpPath,
                    url,
                    "--extract-audio",
                    "--audio-format", "mp3",
                    "-o", tempFilePath
                );
                ytDlpPb.redirectErrorStream(true);
                ExampleExtension.openAPI.printMessage("§e[debug] Starting yt-dlp download...");
                Process ytDlpProcess = ytDlpPb.start();
                String ytDlpOutput = readProcessOutput(ytDlpProcess.getInputStream());
                int ytDlpExit = ytDlpProcess.waitFor();
                ExampleExtension.openAPI.printMessage("§e[debug] yt-dlp exit code: " + ytDlpExit);
                if (ytDlpExit != 0) {
                    ExampleExtension.openAPI.printMessage("§c[yt-dlp audio error]" + ytDlpOutput);
                    return;
                }
                ExampleExtension.openAPI.printMessage("§e[debug] yt-dlp output: " + ytDlpOutput);
                if (!tempFile.exists() || tempFile.length() == 0) {
                    ExampleExtension.openAPI.printMessage("§c[yt-dlp audio error] Downloaded file missing or empty: " + tempFilePath);
                    return;
                }
                // Play with ffplay
                String ffplayPath = System.getenv("APPDATA") + "\\.minecraft\\ffplay.exe";
                ExampleExtension.openAPI.printMessage("§e[debug] Starting ffplay: " + ffplayPath + " " + tempFilePath);
                ProcessBuilder ffplayPb = new ProcessBuilder(
                    ffplayPath,
                    "-autoexit",
                    "-nodisp",
                    tempFilePath
                );
                ffplayPb.redirectErrorStream(true);
                Process ffplayProcess = ffplayPb.start();
                String ffplayOutput = readProcessOutput(ffplayProcess.getInputStream());
                int ffplayExit = ffplayProcess.waitFor();
                ExampleExtension.openAPI.printMessage("§e[debug] ffplay exit code: " + ffplayExit);
                if (ffplayExit != 0) {
                    ExampleExtension.openAPI.printMessage("§c[ffplay audio error]" + ffplayOutput);
                }
                // Do not delete the temp file here; rely on deleteOnExit to ensure ffplay can finish playback.
                isPlaying = false;
                status = "Finished";
                ExampleExtension.openAPI.printMessage("§e[debug] ffplay started, waiting for process to finish...");
                // Auto-play next song if enabled
                if (autoPlay && !playlist.isEmpty() && currentIndex < playlist.size() - 1) {
                    currentIndex++;
                    play(playlist.get(currentIndex));
                }
            } catch (Exception e) {
                status = "Error: " + e.getMessage();
                isPlaying = false;
                ExampleExtension.openAPI.printMessage("§cMusic Player Error: " + e.getMessage());
            }
        });
    }

    // Helper to read process output
    private static String readProcessOutput(java.io.InputStream inputStream) throws Exception {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } finally {
            reader.close();
        }
        return output.toString();
    }
    
    public void nextSong() {
        if (!playlist.isEmpty() && currentIndex < playlist.size() - 1) {
            currentIndex++;
            play(playlist.get(currentIndex));
        }
    }
    
    public void previousSong() {
        if (!playlist.isEmpty() && currentIndex > 0) {
            currentIndex--;
            play(playlist.get(currentIndex));
        }
    }
    
    public void setVolume(int vol) {
        volume = Math.max(0, Math.min(100, vol));
    }
    
    // Getters for widget
    public String getCurrentSong() {
        return currentSong;
    }
    
    public String getCurrentArtist() {
        return currentArtist;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public String getStatus() {
        return status;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public boolean shouldShowWidget() {
        return showWidget;
    }
    
    public void setShowWidget(boolean show) {
        showWidget = show;
    }
    
    public void setAutoPlay(boolean auto) {
        autoPlay = auto;
    }
    
    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    public List<String> getPlaylist() {
        return new ArrayList<>(playlist);
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public int getPlaylistSize() {
        return playlist.size();
    }
}
