import React, { Component } from 'react';
import shaka from 'shaka-player';

import { setVideoStatus } from "../../actions/videoPlayerActions";
import { updateDimensions } from "./Common";

var player;

class ShakaPlayer extends Component {
    constructor(props){
        super(props);

        this.seekTimeoutId_ = null;

        this.playPause = (e) => this._playPause(e);
        this.seekStart = (e) => this._seekStart(e);
        this.seekInput = (e) => this._seekInput(e);
        this.seekEnd = (e) => this._seekEnd(e);
        this.muteClick = (e) => this._muteClick(e);
        this.volumeInput = (e) => this._volumeInput(e);
        this.fullscreenClick = (e) => this._fullscreenClick(e);
        this.currentTimeClick = (e) => this._currentTimeClick(e);
    }

    componentDidMount() {
        this.seekBar_ = document.getElementById("seekBar");
        this.videoContainer_ = document.getElementById("videoContainer");
        this.volumeBar_ = document.getElementById("volumeBar");
        this.muteButton_ = document.getElementById("muteButton");
        this.bufferingSpinner_ = document.getElementById('bufferingSpinner');
        this.currentTime_ = document.getElementById('currentTime');
        this.giantPlayButtonContainer_ = document.getElementById("giantPlayButtonContainer");
        this.playPauseButton_ = document.getElementById("playPauseButton");

        // Install built-in polyfills to patch browser incompatibilities.
		shaka.polyfill.installAll();

		// Check to see if the browser supports the basic APIs Shaka needs.
		if (shaka.Player.isBrowserSupported()) {
		// Everything looks good!
			this.props.video && this.initPlayer();
		} else {
			// This browser does not have the minimum set of APIs we need.
			console.error('Browser not supported!');
        }

        window.addEventListener("resize", updateDimensions);
    }

    componentWillUnmount() {
        clearInterval(this.updateTimeRangeInterval);
        window.removeEventListener("resize", updateDimensions);
        this.player.destroy();
    }

    componentWillReceiveProps(newProps) {
        // if(newProps.video !== null && (!this.props.videoStatus.loaded)){
        //     this.loadVideo(newProps.videoInfo);
        // }

        // new video Info -> load new manifestUri into player
        if(this.props.video && newProps.video){
            if(this.props.video.id !== newProps.video.id){
                const newManifestUri = newProps.video["origin.item.component.url"];
                this.player.load(newManifestUri)
            }
        }
    }

    initPlayer(){
        var me = this;
        var manifestUri = this.props.video["origin.item.component.url"];
        this.video_ = this.refs.video;

        player = new shaka.Player(this.refs.video);

		// Listen for error events.
		player.addEventListener('error', this.onErrorEvent);

		// Try to load a manifest.
		// This is an asynchronous process.
		player.load(manifestUri).then(function() {
			// This runs if the asynchronous load is successful.
            document.getElementById("playPauseButton").innerHTML = "pause";
            me.props.dispatch(setVideoStatus( { ...me.props.videoStatus, playing: true } ));
            updateDimensions();
        }).catch(this.onError);  // onError is executed if the asynchronous load fails.

        this.player = player;

        this.player.addEventListener(
            'buffering', this.onBufferingStateChange_.bind(this));
        this.updateTimeRangeInterval = window.setInterval(this.updateTimeAndSeekRange_.bind(this), 125);

        this.video_.addEventListener(
            'play', this.onPlayStateChange_.bind(this));
        this.video_.addEventListener(
            'pause', this.onPlayStateChange_.bind(this));
      
        // Since videos go into a paused state at the end, Chrome and Edge both fire
        // the 'pause' event when a video ends.  IE 11 only fires the 'ended' event.
        this.video_.addEventListener(
            'ended', this.onPlayStateChange_.bind(this));
      

        this.onVolumeStateChange_();
	}

    onErrorEvent(event) {
		// Extract the shaka.util.Error object from the event.
		this.onError(event.detail);
	}
	
	onError(error) {
		// Log the error.
		console.error('Error code', error.code, 'object', error);
    }

    /**
     * Builds a time string, e.g., 01:04:23, from |displayTime|.
     *
     * @param {number} displayTime
     * @param {boolean} showHour
     * @return {string}
     */
    buildTimeString_(displayTime, showHour) {
        let h = Math.floor(displayTime / 3600);
        let m = Math.floor((displayTime / 60) % 60);
        let s = Math.floor(displayTime % 60);
        if (s < 10) s = '0' + s;
        let text = m + ':' + s;
        if (showHour) {
        if (m < 10) text = '0' + text;
        text = h + ':' + text;
        }
        return text;
    };

    /**
     * Called when the seek range or current time need to be updated.
     */
    updateTimeAndSeekRange_ = function() {
        var seekBar_ = document.getElementById("seekBar"),
            currentTime_ = document.getElementById("currentTime");
      
        let displayTime = this.isSeeking_ ?
            seekBar_.value : this.video_.currentTime;
        let duration = this.video_.duration;
        let bufferedLength = this.video_.buffered.length;
        let bufferedStart = bufferedLength ? this.video_.buffered.start(0) : 0;
        let bufferedEnd =
            bufferedLength ? this.video_.buffered.end(bufferedLength - 1) : 0;
        let seekRange = this.player.seekRange();
        let seekRangeSize = seekRange.end - seekRange.start;
      
        seekBar_.min = seekRange.start;
        seekBar_.max = seekRange.end;
      
        if (this.player.isLive()) {
            // The amount of time we are behind the live edge.
            let behindLive = Math.floor(seekRange.end - displayTime);
            displayTime = Math.max(0, behindLive);
        
            let showHour = seekRangeSize >= 3600;
        
            // Consider "LIVE" when less than 1 second behind the live-edge.  Always
            // show the full time string when seeking, including the leading '-';
            // otherwise, the time string "flickers" near the live-edge.
            if ((displayTime >= 1) || this.isSeeking_) {
                currentTime_.textContent =
                    '- ' + this.buildTimeString_(displayTime, showHour);
                currentTime_.style.cursor = 'pointer';
            } else {
                currentTime_.textContent = 'LIVE';
                currentTime_.style.cursor = '';
            }
        
            if (!this.isSeeking_) {
                this.seekBar_.value = seekRange.end - displayTime;
            }
            } else {
                let showHour = duration >= 3600;
            
                currentTime_.textContent =
                    this.buildTimeString_(displayTime, showHour);
            
                if (!this.isSeeking_) {
                    this.seekBar_.value = displayTime;
                }
            
                currentTime_.style.cursor = '';
            }
        
            let gradient = ['to right'];
            if (bufferedLength === 0) {
            gradient.push('#000 0%');
            } else {
            let clampedBufferStart = Math.max(bufferedStart, seekRange.start);
            let clampedBufferEnd = Math.min(bufferedEnd, seekRange.end);
        
            let bufferStartDistance = clampedBufferStart - seekRange.start;
            let bufferEndDistance = clampedBufferEnd - seekRange.start;
            let playheadDistance = displayTime - seekRange.start;
        
            // NOTE: the fallback to zero eliminates NaN.
            let bufferStartFraction = (bufferStartDistance / seekRangeSize) || 0;
            let bufferEndFraction = (bufferEndDistance / seekRangeSize) || 0;
            let playheadFraction = (playheadDistance / seekRangeSize) || 0;
        
            gradient.push('#000 ' + (bufferStartFraction * 100) + '%');
            gradient.push('#ccc ' + (bufferStartFraction * 100) + '%');
            gradient.push('#ccc ' + (playheadFraction * 100) + '%');
            gradient.push('#444 ' + (playheadFraction * 100) + '%');
            gradient.push('#444 ' + (bufferEndFraction * 100) + '%');
            gradient.push('#000 ' + (bufferEndFraction * 100) + '%');
        }
        seekBar_.style.background =
            'linear-gradient(' + gradient.join(',') + ')';
    };
    
    _playPause(e) {        
        if (!this.video_.duration) {
            // Can't play yet.  Ignore.
            return;
        }

        if (this.video_.paused) {
            this.video_.play();
            this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, playing: true } ));
        } else {
            this.video_.pause();
            this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, playing: false } ));
        }
    }

    onPlayStateChange_ = function() {
        // On IE 11, a video may end without going into a paused state.  To correct
        // both the UI state and the state of the video tag itself, we explicitly
        // pause the video if that happens.
        if (this.video_.ended && !this.video_.paused) {
          this.video_.pause();
          this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, playing: false } ));
        }
      
        // Video is paused during seek, so don't show the play arrow while seeking:
        if (this.video_.paused && !this.isSeeking_) {
            this.playPauseButton_.textContent = 'play_arrow';
            this.giantPlayButtonContainer_.style.display = 'inline';
        } else {
            this.playPauseButton_.textContent = 'pause';
            this.giantPlayButtonContainer_.style.display = 'none';
        }
      };

    
      /**
     * @param {Event} event
     */
    onBufferingStateChange_(event) {
        this.bufferingSpinner_.style.display =
            event.buffering ? 'inherit' : 'none';
    };

    _seekStart(e){
        this.isSeeking_ = true;
        this.video_.pause();
    }

    _seekInput(e){
        if (!this.video_.duration) {
            // Can't seek yet.  Ignore.
            return;
        }
        
        // Update the UI right away.
        this.updateTimeAndSeekRange_();
        
        // Collect input events and seek when things have been stable for 125ms.
        if (this.seekTimeoutId_ != null) {
            window.clearTimeout(this.seekTimeoutId_);
        }
        this.seekTimeoutId_ = window.setTimeout(
            this.onSeekInputTimeout_.bind(this), 125);
    }

    onSeekInputTimeout_() {
        this.seekTimeoutId_ = null;
        this.video_.currentTime = parseFloat(this.seekBar_.value);
    };

    _seekEnd(e){
        if (this.seekTimeoutId_ != null) {
            // They just let go of the seek bar, so end the timer early.
            window.clearTimeout(this.seekTimeoutId_);
            this.onSeekInputTimeout_();
        }

        this.isSeeking_ = false;
        this.video_.play();
    }

    _muteClick(e){
        this.video_.muted = !this.video_.muted;
        this.muteButton_.innerHTML = this.video_.muted ? "volume_off" : "volume_up";

        this.onVolumeStateChange_();
    }

    _volumeInput(e){
        this.video_.volume = parseFloat(this.volumeBar_.value);
        
        if(this.video_.volume === 0){
            this.video_.muted = true;
            this.muteButton_.innerHTML = "volume_off";
        }else{
            this.video_.muted = false;
            this.muteButton_.innerHTML = "volume_up";
        }

        this.onVolumeStateChange_();
    }

    onVolumeStateChange_ = function() {
        if (this.video_.muted) {
          this.muteButton_.textContent = 'volume_off';
          this.volumeBar_.value = 0;
        } else {
          this.muteButton_.textContent = 'volume_up';
          this.volumeBar_.value = this.video_.volume;
        }
      
        let gradient = ['to right'];
        gradient.push('#ccc ' + (this.volumeBar_.value * 100) + '%');
        gradient.push('#000 ' + (this.volumeBar_.value * 100) + '%');
        gradient.push('#000 100%');
        this.volumeBar_.style.background =
            'linear-gradient(' + gradient.join(',') + ')';
    };

    _currentTimeClick = function() {
        // Jump to LIVE if the user clicks on the current time.
        if (this.player.isLive()) {
            this.video_.currentTime = this.seekBar_.max;
        }
    };

    _fullscreenClick(e){
        if (document.fullscreenElement) {
            document.exitFullscreen();
        } else {
            this.videoContainer_.requestFullscreen();
        }
    }

    render() {
        const { video } = this.props;

        return (
            <div id="videoContainer" className="player-container stream-player" style={{ margin: '0 auto' }}>
                <video style={{ width: "100%", height: "100%", margin: "auto" }}
                    ref="video"
                    autoPlay
                    onClick={ this.playPause }>
                </video>
                <div id="giantPlayButtonContainer" className="overlay" onClick={ this.playPause }>
                    <button id="giantPlayButton" className="material-icons">play_arrow</button>
                </div>
                <div id="bufferingSpinner" className="overlay">
                    <svg className="spinnerSvg" viewBox="25 25 50 50">
                        <circle className="spinnerPath" cx="50" cy="50" r="20"
                            fill="none" strokeWidth="2" strokeMiterlimit="10" />
                    </svg>
                </div>
                <div id="castReceiverName" className="overlay"></div>
                <div id="controlsContainer" className="overlay">
                    <div id="controls">
                        <button id="playPauseButton" className="material-icons" onClick={ this.playPause }>play_arrow</button>
                        <label id="liveLabel">&#8226; Live</label>
                        <label htmlFor="seekBar" className="for-screen-readers">seek</label>
                        <input id="seekBar" type="range" step="any" min="0" max="1" defaultValue="0"
                            onMouseDown={ this.seekStart }
                            onTouchStart={ this.seekStart }
                            onInput={ this.seekInput }
                            onTouchEnd={ this.seekEnd }
                            onMouseUp={ this.seekEnd }
                        />
                        <div id="currentTime" onClick={ this.currentTimeClick }>0:00</div>
                        <button id="muteButton" className="material-icons" onClick={ this.muteClick }>volume_up</button>
                        <label htmlFor="volumeBar" className="for-screen-readers">volume</label>
                        <input id="volumeBar" type="range" step="any" min="0" max="1" defaultValue="0" onInput={ this.volumeInput } />
                        <button id="fullscreenButton" className="material-icons" onClick={ this.fullscreenClick }>fullscreen</button>
                    </div>
                </div>
            </div>      
        );
    }
}

export default ShakaPlayer;