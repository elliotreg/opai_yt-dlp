package today.opai.example.commands;

import today.opai.api.features.ExtensionCommand;
import today.opai.example.ExampleExtension;
import today.opai.example.modules.MusicPlayer;

public class MusicCommand extends ExtensionCommand {
    public MusicCommand() {
        super(new String[]{"music", "mp"}, "Music player commands", ".music <play|stop|pause|resume|next|prev|add|clear|list|volume|widget> [args]");
    }

    @Override
    public void onExecute(String[] strings) {
        // Debug: print received arguments
        ExampleExtension.openAPI.printMessage("§7[DEBUG] Command args: " + java.util.Arrays.toString(strings));
        if (MusicPlayer.INSTANCE == null) {
            ExampleExtension.openAPI.printMessage("§cMusic Player module not found!");
            return;
        }

        if (strings.length == 0) {
            printHelp();
            return;
        }

        String command;
        int argOffset = 0;
        // If the first argument is 'music' or 'mp', treat the next as the subcommand
        if (strings[0].equalsIgnoreCase("music") || strings[0].equalsIgnoreCase("mp")) {
            if (strings.length < 2) {
                printHelp();
                return;
            }
            command = strings[1].toLowerCase();
            argOffset = 1;
        } else {
            command = strings[0].toLowerCase();
        }

        switch (command) {
            case "play": {
                if (strings.length < argOffset + 2) {
                    ExampleExtension.openAPI.printMessage("§cUsage: .music play <youtube_url>");
                    return;
                }
                // Join all arguments after the subcommand for the URL
                String url = String.join(" ", java.util.Arrays.copyOfRange(strings, argOffset + 1, strings.length)).trim();
                if (!url.contains("youtube.com") && !url.contains("youtu.be")) {
                    ExampleExtension.openAPI.printMessage("§cPlease provide a valid YouTube URL");
                    return;
                }
                ExampleExtension.openAPI.printMessage("§aPlaying: " + url);
                MusicPlayer.INSTANCE.play(url);
                break;
            }
            case "stop":
                MusicPlayer.INSTANCE.stop();
                ExampleExtension.openAPI.printMessage("§aMusic stopped");
                break;
            case "pause":
                MusicPlayer.INSTANCE.pause();
                ExampleExtension.openAPI.printMessage("§aMusic paused");
                break;
            case "resume":
                MusicPlayer.INSTANCE.resume();
                ExampleExtension.openAPI.printMessage("§aMusic resumed");
                break;
            case "next":
                MusicPlayer.INSTANCE.nextSong();
                ExampleExtension.openAPI.printMessage("§aSkipped to next song");
                break;
            case "prev":
            case "previous":
                MusicPlayer.INSTANCE.previousSong();
                ExampleExtension.openAPI.printMessage("§aSkipped to previous song");
                break;
            case "add": {
                if (strings.length < argOffset + 2) {
                    ExampleExtension.openAPI.printMessage("§cUsage: .music add <youtube_url>");
                    return;
                }
                String addUrl = String.join(" ", java.util.Arrays.copyOfRange(strings, argOffset + 1, strings.length)).trim();
                if (!addUrl.contains("youtube.com") && !addUrl.contains("youtu.be")) {
                    ExampleExtension.openAPI.printMessage("§cPlease provide a valid YouTube URL");
                    return;
                }
                MusicPlayer.INSTANCE.addToPlaylist(addUrl);
                break;
            }
            case "clear":
                MusicPlayer.INSTANCE.clearPlaylist();
                break;
            case "list":
            case "playlist":
                showPlaylist();
                break;
            case "volume":
            case "vol":
                if (strings.length < argOffset + 2) {
                    ExampleExtension.openAPI.printMessage("§aCurrent volume: " + MusicPlayer.INSTANCE.getVolume() + "%");
                    return;
                }
                try {
                    int volume = Integer.parseInt(strings[argOffset + 1]);
                    MusicPlayer.INSTANCE.setVolume(volume);
                    ExampleExtension.openAPI.printMessage("§aVolume set to: " + volume + "%");
                } catch (NumberFormatException e) {
                    ExampleExtension.openAPI.printMessage("§cInvalid volume value. Use a number between 0-100");
                }
                break;
            case "widget":
                if (strings.length < argOffset + 2) {
                    ExampleExtension.openAPI.printMessage("§aWidget visibility: " + (MusicPlayer.INSTANCE.shouldShowWidget() ? "ON" : "OFF"));
                    return;
                }
                String widgetArg = strings[argOffset + 1].toLowerCase();
                if (widgetArg.equals("on") || widgetArg.equals("true")) {
                    MusicPlayer.INSTANCE.setShowWidget(true);
                    ExampleExtension.openAPI.printMessage("§aMusic widget enabled");
                } else if (widgetArg.equals("off") || widgetArg.equals("false")) {
                    MusicPlayer.INSTANCE.setShowWidget(false);
                    ExampleExtension.openAPI.printMessage("§aMusic widget disabled");
                } else {
                    ExampleExtension.openAPI.printMessage("§cUsage: .music widget <on|off>");
                }
                break;
            case "autoplay":
                if (strings.length < argOffset + 2) {
                    ExampleExtension.openAPI.printMessage("§aAutoplay: " + (MusicPlayer.INSTANCE.isAutoPlay() ? "ON" : "OFF"));
                    return;
                }
                String autoArg = strings[argOffset + 1].toLowerCase();
                if (autoArg.equals("on") || autoArg.equals("true")) {
                    MusicPlayer.INSTANCE.setAutoPlay(true);
                    ExampleExtension.openAPI.printMessage("§aAutoplay enabled");
                } else if (autoArg.equals("off") || autoArg.equals("false")) {
                    MusicPlayer.INSTANCE.setAutoPlay(false);
                    ExampleExtension.openAPI.printMessage("§aAutoplay disabled");
                } else {
                    ExampleExtension.openAPI.printMessage("§cUsage: .music autoplay <on|off>");
                }
                break;
            case "status":
                showStatus();
                break;
            case "help":
            default:
                printHelp();
                break;
        }
    }

    private void printHelp() {
        ExampleExtension.openAPI.printMessage("§a=== Music Player Commands ===");
        ExampleExtension.openAPI.printMessage("§7.music play <url> §f- Play a YouTube video");
        ExampleExtension.openAPI.printMessage("§7.music stop §f- Stop playback");
        ExampleExtension.openAPI.printMessage("§7.music pause §f- Pause playback");
        ExampleExtension.openAPI.printMessage("§7.music resume §f- Resume playback");
        ExampleExtension.openAPI.printMessage("§7.music next §f- Next song in playlist");
        ExampleExtension.openAPI.printMessage("§7.music prev §f- Previous song in playlist");
        ExampleExtension.openAPI.printMessage("§7.music add <url> §f- Add to playlist");
        ExampleExtension.openAPI.printMessage("§7.music clear §f- Clear playlist");
        ExampleExtension.openAPI.printMessage("§7.music list §f- Show playlist");
        ExampleExtension.openAPI.printMessage("§7.music volume [0-100] §f- Set/show volume");
        ExampleExtension.openAPI.printMessage("§7.music widget <on|off> §f- Toggle widget");
        ExampleExtension.openAPI.printMessage("§7.music autoplay <on|off> §f- Toggle autoplay");
        ExampleExtension.openAPI.printMessage("§7.music status §f- Show current status");
    }

    private void showPlaylist() {
        if (MusicPlayer.INSTANCE.getPlaylistSize() == 0) {
            ExampleExtension.openAPI.printMessage("§cPlaylist is empty");
            return;
        }

        ExampleExtension.openAPI.printMessage("§a=== Playlist (" + MusicPlayer.INSTANCE.getPlaylistSize() + " songs) ===");
        for (int i = 0; i < MusicPlayer.INSTANCE.getPlaylist().size(); i++) {
            String marker = (i == MusicPlayer.INSTANCE.getCurrentIndex()) ? "§a► " : "§7  ";
            ExampleExtension.openAPI.printMessage(marker + (i + 1) + ". §f" + MusicPlayer.INSTANCE.getPlaylist().get(i));
        }
    }

    private void showStatus() {
        ExampleExtension.openAPI.printMessage("§a=== Music Player Status ===");
        ExampleExtension.openAPI.printMessage("§7Status: §f" + MusicPlayer.INSTANCE.getStatus());
        ExampleExtension.openAPI.printMessage("§7Current Song: §f" + MusicPlayer.INSTANCE.getCurrentSong());
        ExampleExtension.openAPI.printMessage("§7Artist: §f" + MusicPlayer.INSTANCE.getCurrentArtist());
        ExampleExtension.openAPI.printMessage("§7Volume: §f" + MusicPlayer.INSTANCE.getVolume() + "%");
        ExampleExtension.openAPI.printMessage("§7Playlist: §f" + MusicPlayer.INSTANCE.getPlaylistSize() + " songs");
        ExampleExtension.openAPI.printMessage("§7Widget: §f" + (MusicPlayer.INSTANCE.shouldShowWidget() ? "ON" : "OFF"));
        ExampleExtension.openAPI.printMessage("§7Autoplay: §f" + (MusicPlayer.INSTANCE.isAutoPlay() ? "ON" : "OFF"));
    }
}
