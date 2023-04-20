<img align="right" src=".github/image.png" height="200" width="200" alt="Project icon">

<h1>Kvintakord</h1>

<h2>A desktop music player written in Kotlin</h2>

Some of its features include:
- Playing music from YouTube, Spotify, SoundCloud [and more](https://github.com/Walkyst/lavaplayer-fork#supported-formats)
- Search on YouTube, Spotify and SoundCloud
- Ordinary music controls
- Audio output device selection
- A queue system
- Closing to the system tray, while maintaining playback
- Precise seeking support
- Low memory footprint

Kvintakord is written in pure Kotlin, using the [TornadoFX](https://tornadofx.io) framework for the GUI and [Lavaplayer](https://github.com/Walkyst/lavaplayer-fork) for the audio.
This project is a successor of [MusicGods](https://github.com/PattexPattex/MusicGods), a Discord music bot, created and maintained by me.

<h2>Table of contents</h2>

<!-- TOC -->
  * [Using Kvintakord](#using-kvintakord)
    * [Launching from desktop](#launching-from-desktop)
    * [Spotify on Kvintakord](#spotify-on-kvintakord)
  * [Submitting an issue](#submitting-an-issue)
  * [Editing, building and contributing](#editing-building-and-contributing)
<!-- TOC -->

## Using Kvintakord

Please use Java 17 or higher. I recommend [Eclipse Temurin](https://adoptium.net/temurin/releases/). Download the latest release from the [releases page](https://github.com/PattexPattex/Kvintakord/releases).
It is recommended to download the .zip file, as it is the most straightforward way to run Kvintakord. Extract its contents to a safe place.

<details>
    <summary>Windows</summary>
    
Simply double-click the `Kvintakord.bat` file in the `bin` directory.
You could also start it in the terminal.
</details>

<details>
    <summary>Linux</summary>

Open the `bin` directory in the terminal and execute `./Kvintakord`.
</details>

<details>
    <summary>Mac</summary>

*`¯\_(ツ)_/¯`*
</details>

### Launching from desktop

You can create a shortcut to launch Kvintakord directly from your desktop.

<details>
    <summary>Windows</summary>
    
1. Right-click on the `Kvintakord.bat` file and select `Create shortcut`.
2. Right-click on the shortcut, select `Properties` and `Change Icon`. In the prompt, select `Browse...` and find the `icon.ico` included in the installation. Then, press OK.
3. Rename the shortcut to `Kvintakord` or something like that and move it to your desktop.
4. That's it!
</details>

<details>
    <summary>Linux</summary>

1. Create a file `kvintakord.desktop` on your desktop and open it with your favorite text editor.
2. Paste the following into the file (replace `APP_HOME` with the absolute path of the `bin` directory in your Kvintakord installation, e.g.:`/home/pattexpattex/Kvintakord`):

```shell
#!/usr/bin/env xdg-open
[Desktop Entry]
Type=Application
Terminal=false
Exec=APP_HOME/bin/Kvintakord
Name=Kvintakord
Icon=APP_HOME/icon.png
```

3. Save the file and close the text editor.
4. Right-click on the file and click the `Allow Launching` option. If the option doesn't appear, you probably didn't replace `APP_HOME` correctly.
5. You're all set!
</details>

<details>
    <summary>Mac</summary>

*`¯\_(ツ)_/¯`*
</details>

### Spotify on Kvintakord

Kvintakord supports Spotify as an audio source. To enable it, go to the `Search` tab an open a dialogue with the `Update Spotify credentials` button. 
Note that you don't need to enter your username and password, but an ID and the secret of a Spotify application. A guide on how to create one can be found [here](https://developer.spotify.com/documentation/web-api/concepts/apps).
_The credentials are stored in a file, so, that's that._

## Submitting an issue

**Before submitting anything, please check the issues list for any similar issues.**
If you have a suggestion or need help, please start a discussion.
If you would like to suggest a feature or file a bug report, open an issue.

## Editing, building and contributing

After importing the project, simply execute `gradlew build` in the command line.

If you want to edit the source code, please do so under the conditions of the MIT license.

If you wish to contribute code to this project, please open a pull request with details of your contribution.
