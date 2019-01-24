import React, { Component } from "react";
import { connect } from "react-redux";
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import VideoPlayerHolder from './VideoPlayerStyle';
import ReactVideoPlayer from './ReactPlayer';
import ShakaPlayer from './ShakaPlayer';

import { setVideoStatus, setVideoInfo } from "../../actions/videoPlayerActions";

var Player = {};
class VideoPlayer extends Component {
    componentWillReceiveProps(newProps) {
        //Video info removed - unload video
        if(!newProps.videoInfo && this.props.videoInfo) {
            this.unloadVideo();
        }

        //If video not loaded and it has new video Info -> load video
        if(newProps.videoInfo !== null && (!this.props.videoStatus.loaded)){
            this.loadVideo(newProps.videoInfo);
        }

        //If video is not playing, and view is not docked (so is fixed) -> unload video
        if(newProps.videoStatus && ( newProps.videoStatus.loaded === true )
            && ( newProps.videoStatus.playing === false) 
            &&  (newProps.videoStatus.docked === false )){
            this.unloadVideo();
        }

        if(this.props.videoInfo && newProps.videoInfo){
            var currentType = this.props.videoInfo['content-type'] === '/component/video' ? "video" : "stream",
                newType = newProps.videoInfo['content-type'] === '/component/video' ? "video" : "stream";

            //If new props contains a different type of video than current (stream, video) => load new one
            if( currentType !== newType ){
                this.unloadVideo();
                this.loadVideo(newProps.videoInfo);
            }
        }
    }

    loadVideo(videoInfo) {
        var videoType = videoInfo['content-type'] === '/component/video' ? "video" : "stream";

        Player = videoType === "video" ? ReactVideoPlayer : ShakaPlayer;
        this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, loaded: true } ));
    }

    unloadVideo() {
        this.props.dispatch(setVideoStatus( { ...this.props.videoStatus, loaded: false, playing: false } ));
        this.props.dispatch(setVideoInfo( null ));
    }

    render() {
        return (
            <VideoPlayerHolder>
                <div id="app-content__player" className="app-content__player">
                    { this.props.videoInfo && this.props.videoStatus.loaded &&
                        <div className="app-content__player-wrapper">
                            <div className={ `global-video-player global-video-player--visible global-video-player--${ this.props.videoStatus.docked ? 'docked' : 'fixed' }` }>
                                <div id="videoPlayerAspect" className="global-video-player__aspect">
                                    <div className="global-video-player__inner">
                                        <Player video={ this.props.videoInfo }
                                            videoStatus={ this.props.videoStatus }
                                            dispatch={ this.props.dispatch }
                                            controls={ true }
                                        />

                                        <Link className="fixed-player__link-overlay" to={ this.props.videoStatus.currentVideoUrl }>
                                        </Link>

                                        <a className="global-video-player__close" onClick={ this.unloadVideo.bind(this) }>
                                            <FontAwesomeIcon icon={ faTimes }/>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    }
                </div>
            </VideoPlayerHolder>
        );
    }
}

function mapStateToProps(store) {
    return { 
        videoInfo: store.video.videoInfo,
        videoStatus: store.video.videoStatus
    };
}

export default connect(mapStateToProps)(VideoPlayer);