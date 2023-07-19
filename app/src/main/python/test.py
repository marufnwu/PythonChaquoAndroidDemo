from pytube import YouTube
from pytube import StreamQuery
from pytube import Stream
from pytube import extract
from pytube.exceptions import RegexMatchError



#print (StreamQuery(yt.fmt_streams).filter(type="video"))
#print (extract.get_ytcfg(yt.watch_html))



def getStremData(url):
    try:
        yt = YouTube(url=url, allow_oauth_cache=True, use_oauth=False)
        #yt.bypass_age_gate()
        if yt.age_restricted:
            print("Age restricted video")
        else:
            varTest  = yt.streaming_data
    except KeyError:
        return "Url not valid"
    else:
        return varTest
    
print (getStremData("https://www.youtube.com/watch?v=7RJk23Wr_xs"))