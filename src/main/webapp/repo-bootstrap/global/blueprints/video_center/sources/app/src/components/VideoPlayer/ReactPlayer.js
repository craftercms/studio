import React, { Component } from 'react';
import ReactPlayer from 'react-player';
import { setVideoStatus } from "../../actions/videoPlayerActions";
import { updateDimensions } from "./Common";
import fscreen from 'fscreen';

class ReactVideoPlayer extends Component {
    state = {
        url: null,
        playing: true,
        volume: 0.8,
        muted: false,
        played: 0,
        loaded: 0,
        playedSeconds: '0:00',
        duration: 0
    }

    componentDidMount() {
        window.addEventListener("resize", updateDimensions);
    }

    componentWillUnmount() {
        window.removeEventListener("resize", updateDimensions);
    }

    // Custom controls methods
    playPause = () => {
        this.setState({ playing: !this.state.playing })
    }

    onClickFullscreen = () => {
        var videoContainer_ = document.getElementById("videoContainer");

        if (fscreen.fullscreenEnabled) {
            if(fscreen.fullscreenElement){
                fscreen.exitFullscreen();
            }else{
                fscreen.requestFullscreen(videoContainer_);
            }
        }

    }

    onSeekMouseDown = e => {
        this.setState({ seeking: true })
    }
    onSeekChange = e => {
        this.setState({ played: parseFloat(e.target.value) })
    }
    onSeekMouseUp = e => {
        this.setState({ seeking: false })
        this.player.seekTo(parseFloat(e.target.value))
    }
    onProgress = state => {
        let playedSeconds = state.playedSeconds;

        playedSeconds = Math.round(playedSeconds)

        // Hours, minutes and seconds
        var hrs = ~~(playedSeconds / 3600);
        var mins = ~~((playedSeconds % 3600) / 60);
        var secs = playedSeconds % 60;

        // Output like "1:01" or "4:03:59" or "123:03:59"
        var formatted = "";

        if (hrs > 0) {
            formatted += "" + hrs + ":" + (mins < 10 ? "0" : "");
        }

        formatted += "" + mins + ":" + (secs < 10 ? "0" : "");
        formatted += "" + secs;
    
        state.playedSeconds = formatted;

        // We only want to update time slider if we are not currently seeking
        if (!this.state.seeking) {
            this.setState(state)
        }
    }
    setVolume = e => {
        const volume = parseFloat(e.target.value);
        this.setState({ volume: volume })
    }
    toggleMuted = (e) => {
        var muted = !this.state.muted,
            volume;

        this.setState({ muted });

        if(muted){
            this.previousVol = this.state.volume;
            volume = 0;
        }else{
            volume = this.previousVol;
        }

        this.setState({ volume })
    }

    //////////////////////////

    onStart(e) {
        updateDimensions();

        if( this.props.seekTo ){
            this.refs.video.seekTo(this.props.seekTo);
        }
    }

    onPlaying(e) {
        this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, playing: true } ));
        this.setState({ playing: true })
    }

    onStopped(e){
        this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, playing: false } ));
        this.setState({ playing: false })
    }

    ref = player => {
        this.player = player
    }
    render() {
        const { video } = this.props;
        const { playing, volume, muted, played, loaded, playedSeconds } = this.state

        return (
            <div id="videoContainer" className="player-container stream-player" style={{ margin: '0 auto' }}>
                <ReactPlayer 
                    className = "video-player"
                    controls = { false }
                    url = { `https://www.youtube.com/watch?v=${ video.youTubeVideo }` }
                    playing = { playing }
                    ref = {this.ref}
                    width = "100%"
                    height = "100%"
                    volume = { volume }
                    muted = { muted }
                    onReady = { (e) => { this.onStart(e) } }
                    onPlay = { (e) => { this.onPlaying(e) } }
                    onPause = { (e) => { this.onStopped(e) } }
                    onEnded = { (e) => { this.onStopped(e) } }
                    onProgress = { (e) => { this.onProgress(e) } }
                />

                <div id="controlsContainer" className="overlay">
                    <div id="controls">
                        <button id="playPauseButton" className="material-icons" onClick={ this.playPause }>{playing ? 'pause' : 'play_arrow'}</button>
                        <label htmlFor="seekBar" className="for-screen-readers">seek</label>
                        <input id="seekBar" type="range" step="any" min="0" max="1" 
                            value={played}
                            onMouseDown={this.onSeekMouseDown}
                            onChange={this.onSeekChange}
                            onMouseUp={this.onSeekMouseUp}
                            style= {{ background: `linear-gradient(to right, 
                                rgb(0, 0, 0) 0%, 
                                rgb(204, 204, 204) 0%, 
                                rgb(204, 204, 204) ${ played * 100 }%, 
                                rgb(68, 68, 68) ${ played * 100 }%, 
                                rgb(68, 68, 68) ${ loaded * 100 }%, 
                                rgb(0, 0, 0) ${ loaded * 100 }%)` 
                            }}
                        />
                        <div id="currentTime" onClick={ this.currentTimeClick }>{ playedSeconds }</div>
                        <button id="fastForwardButton" className="material-icons">fast_forward</button>
                        <button id="muteButton" className="material-icons" onClick={ this.toggleMuted }>{muted ? 'volume_off' : 'volume_up'}</button>
                        <label htmlFor="volumeBar" className="for-screen-readers">volume</label>
                        <input id="volumeBar" type='range' min="0" max="1" step='any' value={volume} onChange={this.setVolume} 
                        style={{ background: `linear-gradient(to right, rgb(204, 204, 204) ${ volume * 100 }%, rgb(0, 0, 0) ${ volume * 100 }%, rgb(0, 0, 0) 100%)` }}/>
                        <button id="fullscreenButton" className="material-icons" onClick={ this.onClickFullscreen }>fullscreen</button>
                    </div>
                </div>
            </div>
        );
    }
}

export default ReactVideoPlayer;