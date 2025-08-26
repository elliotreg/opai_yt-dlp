package today.opai.example.widgets;

import java.awt.Color;

import today.opai.api.features.ExtensionWidget;
import today.opai.example.ExampleExtension;
import today.opai.example.modules.MusicPlayer;

public class MusicPlayerWidget extends ExtensionWidget {
    public MusicPlayerWidget() {
        super("Music Player");
    }

    @Override
    public void render() {
        if (MusicPlayer.INSTANCE == null || !MusicPlayer.INSTANCE.shouldShowWidget()) {
            return;
        }

        int height = 0;
        int renderWidth = 120; // Minimum width

        // Background
        ExampleExtension.openAPI.getRenderUtil().drawRoundRect(getX(), getY(), getWidth(), getHeight(), 5, new Color(0, 0, 0, 150));

        height += 10;

        // Title
        String title = "♪ Music Player";
        ExampleExtension.openAPI.getFontUtil().getTahoma18().drawCenteredString(title, getX() + (this.getWidth() / 2) - 1, getY() + 4, -1);
        renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(title), renderWidth);

        // Status
        String status = MusicPlayer.INSTANCE.getStatus();
        String statusText = "Status: " + status;
        ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(statusText, getX() + 3, getY() + (height += 10), getStatusColor(status));
        renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(statusText), renderWidth);

        // Current song info
        if (MusicPlayer.INSTANCE.isPlaying() || !MusicPlayer.INSTANCE.getCurrentSong().isEmpty()) {
            String song = MusicPlayer.INSTANCE.getCurrentSong();
            String artist = MusicPlayer.INSTANCE.getCurrentArtist();
            
            if (!song.isEmpty()) {
                // Truncate long song names
                if (song.length() > 25) {
                    song = song.substring(0, 22) + "...";
                }
                String songText = "♫ " + song;
                ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(songText, getX() + 3, getY() + (height += 10), 0xFFFFFFFF);
                renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(songText), renderWidth);
            }
            
            if (!artist.isEmpty()) {
                // Truncate long artist names
                if (artist.length() > 25) {
                    artist = artist.substring(0, 22) + "...";
                }
                String artistText = "by " + artist;
                ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(artistText, getX() + 3, getY() + (height += 10), 0xFFAAAAAA);
                renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(artistText), renderWidth);
            }
        }

        // Volume
        String volumeText = "Volume: " + MusicPlayer.INSTANCE.getVolume() + "%";
        ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(volumeText, getX() + 3, getY() + (height += 10), 0xFFCCCCCC);
        renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(volumeText), renderWidth);

        // Playlist info
        if (MusicPlayer.INSTANCE.getPlaylistSize() > 0) {
            String playlistText = "Playlist: " + (MusicPlayer.INSTANCE.getCurrentIndex() + 1) + "/" + MusicPlayer.INSTANCE.getPlaylistSize();
            ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(playlistText, getX() + 3, getY() + (height += 10), 0xFFCCCCCC);
            renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(playlistText), renderWidth);
        }

        // Controls hint
        if (MusicPlayer.INSTANCE.isPlaying()) {
            String controlsText = "Use .music commands";
            ExampleExtension.openAPI.getFontUtil().getTahoma18().drawString(controlsText, getX() + 3, getY() + (height += 10), 0xFF888888);
            renderWidth = Math.max(ExampleExtension.openAPI.getFontUtil().getTahoma18().getWidth(controlsText), renderWidth);
        }

        setWidth(renderWidth + 8);
        setHeight(height + 12);
    }

    @Override
    public boolean renderPredicate() {
        return MusicPlayer.INSTANCE != null && 
               MusicPlayer.INSTANCE.isEnabled() && 
               MusicPlayer.INSTANCE.shouldShowWidget();
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "playing":
                return 0xFF00FF00; // Green
            case "paused":
                return 0xFFFFFF00; // Yellow
            case "loading...":
                return 0xFF00FFFF; // Cyan
            case "stopped":
            case "finished":
                return 0xFFFFFFFF; // White
            case "disabled":
                return 0xFF888888; // Gray
            default:
                if (status.startsWith("Error")) {
                    return 0xFFFF0000; // Red
                }
                return 0xFFFFFFFF; // White
        }
    }
}
