package com.example.pythonchaquoandroiddemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pythonchaquoandroiddemo.models.ytdlp.YtdlpData
import com.google.gson.Gson


@UnstableApi class MainActivity : AppCompatActivity() {
    lateinit var playerView: PlayerView
    lateinit var trackButton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView);
        trackButton = findViewById(R.id.select_tracks_button);


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()


        //val player = ExoPlayer.Builder(this)


        //val testModule = py.getModule("test")

        val ytdlpModule = py.getModule("ytdl")

//        val value = testModule.callAttr("getStremData", "https://www.youtube.com/watch?v=7RJk23Wr_xs&t=123s")
//
//        try {
//            val jsonStreamData = Gson().fromJson(value.toString(), StreamData::class.java)
//            for (format in jsonStreamData.formats){
//                Log.d("VideoFormat", "info: "+format.qualityLabel+" type "+format.mimeType+" url "+format.url)
//            }
//
//            for (format in jsonStreamData.adaptiveFormats){
//                Log.d("VideoFormat", "Adaptive info: "+format.qualityLabel+" type "+format.mimeType+" url "+format.url)
//            }
//        }catch (e:Exception){
//            e.printStackTrace()
//        }


        val value =  ytdlpModule.callAttr("getVideoInfo", "https://www.youtube.com/watch?v=7RJk23Wr_xs")

        var manifest:String? = null;

        try {
            val jsonStreamData = Gson().fromJson(value.toString(), YtdlpData::class.java)
            Log.d("VideoFormat", "count: " + jsonStreamData.formats.size)
            for (format in jsonStreamData.formats) {
                if (format.manifest_url != null)

                    if(manifest==null){
                        manifest = format.manifest_url
                    }else{
                        break
                    }


            }
        } catch (e: Exception) {
            Log.d("VideoFormat", "count: " + e.message)

            e.printStackTrace()
        }



        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)
        val trackSelector = DefaultTrackSelector(this)
        trackSelector.parameters = trackSelector.buildUponParameters().build()


        val player = ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector).build()

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

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(manifest!!))


        player.addMediaSource(mediaSource!!)

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


    }


}