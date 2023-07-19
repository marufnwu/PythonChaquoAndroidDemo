import yt_dlp

def extract_dash_manifest(video_url):
    ydl_opts = {
        'quiet': True,  # Suppress console output
        'format': 'best',  # Download the best quality available to get the DASH manifest
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info_dict = ydl.extract_info(video_url, download=False)
        formats = info_dict.get('formats', [])
        dash_manifest_url = None

        for format_info in formats:
            if format_info.get('format_note', '') == 'DASH audio':
                # DASH manifest is available in a separate format note
                dash_manifest_url = format_info.get('url', None)
                break

        return dash_manifest_url

if __name__ == "__main__":
    video_url = "https://www.youtube.com/watch?v=nICXtaOxmxE"
    dash_manifest_url = extract_dash_manifest(video_url)

    if dash_manifest_url:
        print("DASH Manifest URL:", dash_manifest_url)
    else:
        print("DASH Manifest not found for the video.")
