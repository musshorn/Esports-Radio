import cherrypy
import os
import requests
import json
import re
import subprocess
import threading

USHER_API = 'http://usher.twitch.tv/select/{channel}.json?nauthsig={sig}&nauth={token}&allow_source=true'
TOKEN_API = 'http://api.twitch.tv/api/channels/{channel}/access_token'
HOSTING_DOMAIN = 'http://musshorn.me/'  #Path where the m3u8 file and the TS files will be served from
active_games = set()

def get_token_and_signature(channel):
  url = TOKEN_API.format(channel=channel)
  r = requests.get(url)
  txt = r.text.encode("utf8")
  data = json.loads(txt)
  sig = data['sig']
  token = data['token']
  return token, sig
 
def get_live_stream(channel):
    token, sig = get_token_and_signature(channel)
    url = USHER_API.format(channel=channel, sig=sig, token=token)
    r = requests.get(url)
    txt = r.text
    for line in txt.split('\n'):
      if re.match('https?://.*', line):
        return line

class EncoderThread(threading.Thread):
  def __init__(self, game, url):
    threading.Thread.__init__(self)
    self.game = game
    self.url = url

  def run(self):
    subprocess.call('ffmpeg -i "' + self.url + '" -acodec libmp3lame -ab 128k -vn -map 0:0 -f ssegment -segment_list ' + self.game + '.m3u8 -segment_list_size 5 -segment_wrap 15 -segment_time 10 ' + self.game+ '_out%03d.ts',stdout=subprocess.PIPE, stderr=subprocess.PIPE) #works using 127.0.0.1 only?
    active_games.remove(self.game)


class Server(object):
    def index(self, **params):
        playlist = get_live_stream(params['game'])
        active_games.add(params['game'])
        g = EncoderThread(params['game'], playlist)
        g.start()
        return HOSTING_DOMAIN + params['game'] + ".m3u8"
    index.exposed = True

cherrypy.config.update({'server.socket_host': '0.0.0.0'})
cherrypy.config.update({'server.socket_port': int(os.environ.get('PORT', '5000'))})
cherrypy.quickstart(HelloWorld())