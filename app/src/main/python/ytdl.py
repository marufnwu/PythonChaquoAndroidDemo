import yt_dlp 
import json

opts = {'format':'best', 'quiet': True,}
dlp = yt_dlp.YoutubeDL(opts)


def getVideoInfo(url):
    try:
       info = dlp.extract_info(url=url, download=False)
    except KeyError:
        return "Url not valid"
    else:
        return json.dumps(dlp.sanitize_info(info))

#print(getVideoInfo("https://www.youtube.com/watch?v=fDticmXbG0I"))    
