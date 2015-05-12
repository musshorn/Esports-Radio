# Esports Radio

This is a small android app and server I wrote in late 2014 to provide and audio only stream of twitch.tv streams. I originally wrote it for just dota streams and planned to extend it with dota specific features but realised there was greater potential to just serve any twitch streams.

I don't have time to continue the project sadly. If you want to continue it, or build upon it, this should give you a good starting point.

## Installation
---
### Server
**You will need:**
- Python 2.7 with Cherrypy installed
- FFmpeg for your respective operating system
- A web server set up for serving static files from a directory
 
The server is also split into 2 in effect. You need a static file service (I've used nginx) and the python app which serves the URLs for the audio streams to the phone app.
The server can be any windows or linux machine with FFmpeg installed. For windows installs, download the static FFmpeg build and put the FFmpeg binary in the same folder as main.py
So if your web server was serving files from C:\Web you should also put main.py and FFmpeg in the same folder.

Once that's setup note the URL at the top of main.py. This will need to match in the app code later on

### Android APP
**You will need:**
- Android Studio
 
You then need to edit line 62 of StreamService.java with the domain or IP where the app is running. Once that is set you can simply build it and it should all work.

## Libraries
----
I would like to shout out to the following project and libraries that made this all possible.
- wseemann with his [fantastic MediaPlayer replacement](https://github.com/wseemann/FFmpegMediaPlayer). The default media player on android really sucks for streaming. This is probably overkill but provided the best results of the media players I tested.
- [The Android Async HTTP Library](https://github.com/loopj/android-async-http). This is used to load in the stream images without locking up the UI while it waits for them to load.
- [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader) speeds up the UI somewhat by caching the images that have been loaded in