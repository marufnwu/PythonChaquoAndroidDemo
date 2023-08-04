package com.example.pythonchaquoandroiddemo

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pythonchaquoandroiddemo.models.StreamData
import com.google.gson.Gson


@UnstableApi class MainActivity : AppCompatActivity() {
    lateinit var playerView: PlayerView
    lateinit var trackButton:Button
    private val url  ="https://www.youtube.com/watch?v=iAXymug9VvQ"

    //Minimum Video you want to buffer while Playing
    val MIN_BUFFER_DURATION = 2000

    //Max Video you want to buffer during PlayBack
    val MAX_BUFFER_DURATION = 5000

    //Min Video you want to buffer before start Playing it
    val MIN_PLAYBACK_START_BUFFER = 1500

    //Min video You want to buffer when user resumes video
    val MIN_PLAYBACK_RESUME_BUFFER = 2000
    private lateinit var downloderModule : PyObject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView);
        trackButton = findViewById(R.id.select_tracks_button);


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()

        var manifest:String? = null;
        //val player = ExoPlayer.Builder(this)


        val testModule = py.getModule("test")
        downloderModule = py.getModule("downloader")

        //val ytdlpModule = py.getModule("ytdl")

        val value = testModule.callAttr("getStremData", "https://www.youtube.com/watch?v=22ZsVw4Uz4Y")
        var audioSource :String? = null
        var source2 :String ? = null
        try {
            val jsonStreamData = Gson().fromJson(value.toString(), StreamData::class.java)
            for (format in jsonStreamData.formats){
                Log.d("VideoFormat", "info: "+format.qualityLabel+" type "+format.mimeType+" url "+format.url)
            }

            manifest = jsonStreamData.adaptiveFormats[3].url
            source2 = jsonStreamData.adaptiveFormats[4].url

            for (format in jsonStreamData.adaptiveFormats){
                if(format.mimeType.contains("webm")){
                    Log.d("AdaptiveVideoFormat", "Adaptive info: "+format.qualityLabel+" type "+format.mimeType+" url "+format.url)
                    if(manifest==null)
                        manifest = format.url
                }

                if(format.mimeType.contains("audio")){
                    audioSource = format.url
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }


        //val value =  ytdlpModule.callAttr("getVideoInfo", "https://www.youtube.com/watch?v=7RJk23Wr_xs")



//        try {
//            val jsonStreamData = Gson().fromJson(value.toString(), YtdlpData::class.java)
//            Log.d("VideoFormat", "count: " + jsonStreamData.formats.size)
//            for (format in jsonStreamData.formats) {
//                if (format.manifest_url != null)
//
//                    if(manifest==null){
//                        manifest = format.manifest_url
//                    }else{
//                        break
//                    }
//
//
//            }
//        } catch (e: Exception) {
//            Log.d("VideoFormat", "count: " + e.message)
//
//            e.printStackTrace()
//        }



        val dataSourceFactory: DataSource.Factory = CacheDataSource.Factory()

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)
        val trackSelector = DefaultTrackSelector(this)
        trackSelector.parameters = trackSelector.buildUponParameters().build()

        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                MIN_BUFFER_DURATION,
                MAX_BUFFER_DURATION,
                MIN_PLAYBACK_START_BUFFER,
                MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true).build()


        val player = ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector).setLoadControl(loadControl).build()

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.d("Exoplayer", "onPlayerError: " + error.message)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log.d("Exoplayer", "onPlayerError: " + error!!.message)
            }

        })

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(manifest!!))

        val mediaSource1 = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(source2!!))

        val audioMediaSource1 = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(audioSource!!))

        val mergingMediaSource = MergingMediaSource(mediaSource, audioMediaSource1)






        player.addMediaSource(mergingMediaSource)

        playerView.player = player
        player.prepare()
        player.play()


        trackButton.setOnClickListener {
            val trackSelectionDialog = TrackSelectionDialog.createForPlayer(
                player
            ){ dismissedDialog ->

            }
            trackSelectionDialog.show(supportFragmentManager,  null)

        }

        findViewById<Button>(R.id.download)
            .setOnClickListener {
                downloadVideo(url, "360p")
            }


    }

    fun onCallback(event: String) {
        Log.d("PythonCallback", "onCallback: "+event)
    }

    private fun downloadVideo(url:String, quality:String){
        val downlaodDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val downloader = downloderModule.callAttr("downloader", url, "mp4", downlaodDir?.absolutePath,
            "480p", downlaodDir?.path )

        downloader.put("hook", ::onCallback)
        val download = downloader.callAttr("download");
    }


}