from __future__ import unicode_literals
import yt_dlp as youtube_dl
import os
import time
from threading import *
import sys
import json

class downloader:
    def __init__(self,url, dl_format, directory, max_best_quality):
        self.url = url
        self.dl_format = dl_format
        self.directory = directory
        self.max_best_quality = max_best_quality
        self.fail = False
        self.stop=False
        self.status = 'Stopped'
        self.hook = None


    def download(self):
        self.status='Preparing'
        self.process = Thread(target=self.downloadAction,args=(self.directory,self.dl_format, self.max_best_quality,self.url))
     
        try:
            self.process.start()
            return True
        except Exception as e:
            if "not a valid URL" in str(e):
                self.fail = True
                return False
            time.sleep(10)
            self.fail = True
            return False
            
    def downloadAction(self,directory, dl_format, max_best_quality, url):
        
        ydl_opts = {}
        if directory[-1] != "/":    
            directory+="/"
        if dl_format == "mp4":
            ydl_opts = {
                'format': 'best[height<='+max_best_quality+']',
                'outtmpl': directory+"%(title)s.%(ext)s",
                'progress_hooks': [self.my_hook],
            }
        elif dl_format == "m4a":
            ydl_opts = {
                'format': 'bestaudio[ext=m4a]',
                'outtmpl': directory+"%(title)s.%(ext)s",
                'progress_hooks': [self.my_hook],
            }
        try:
            with youtube_dl.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
        except ValueError:
            self.fail = True
        except Exception as e:
            time.sleep(5)
            try:
                with youtube_dl.YoutubeDL(ydl_opts) as ydl:
                    print("retrying....")
                    ydl.download([url])
            except Exception as e:
                print("error fff",str(e))
                self.fail = True
                
                
            
    def my_hook(self, d):
        #inside d (type DICT) variable various information is avialable for downloading video
        print("Hook", d)
        print("gui_callback", self.hook)
        if self.hook:
            self.hook.onPercentage(d['_percent_str'])

        if d['status'] == 'downloading':
            self.status=d['_percent_str']
            if self.stop==True:
                self.status='Stopped'
                raise ValueError('Stopping process')
    def isFail(self):
        return self.fail  
    def state(self):
        return self.process.is_alive()
    def stop(self):
        self.stop=True


