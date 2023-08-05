import yt_dlp as youtube_dl

def download_video(url, format_code, output_path="."):
    ydl_opts = {
        'format': format_code,
        'outtmpl': f"{output_path}/%(title)s.%(ext)s",
    }

    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])

if __name__ == "__main__":
    video_url = "https://www.youtube.com/watch?v=7RJk23Wr_xs"
    desired_format_code = "22"  # Format code for the desired video format (e.g., 22 for 720p mp4)

    download_video(video_url, desired_format_code)
