import React, { Component } from "react";
import { connect } from "react-redux";
import { isNullOrUndefined } from "util";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShareAlt, faComment } from "@fortawesome/free-solid-svg-icons";

import { getItem, search } from '@craftercms/redux';
import { SearchService } from '@craftercms/search';

import NotFound from '../Errors/404';
import VideoCategories from '../../components/VideoCategories/VideoCategories';
import VideoHolder from './VideoStyle';
import Slider from '../../components/Slider/Slider';
import ModalDialog from '../../components/Modal/Modal';
// import VideoSidebar from './VideoSidebar.js';
import { setVideoInfo, setVideoStatus } from "../../actions/videoPlayerActions";
import { setHeaderGhost } from '../../actions/headerActions';
import { pageScrollTop } from '../../utils';

class Video extends Component {
    state = {
        notFound: false
    }

    constructor(props) {
        super(props);

        this.loadVideo(props);
    }

    componentWillMount() {  
        this.props.setVideoStatus( { ...this.props.videoStatus, docked: true, currentVideoUrl: this.props.match.url } );
    }

    componentWillUnmount() {
        this.props.setHeaderGhost(false);
    }

    componentWillReceiveProps(newProps){
        var { match, searchResults } = this.props,
            searchEntry = searchResults.entries[this.searchId],
            newSearchEntry = newProps.searchResults.entries[this.searchId];

        if(match.url !== newProps.match.url){
            this.loadVideo(newProps);
            pageScrollTop();
        }

        if( 
            ( isNullOrUndefined(searchEntry) && !isNullOrUndefined(newSearchEntry) )
            || ( !isNullOrUndefined(searchEntry) && !isNullOrUndefined(newSearchEntry))
            && newProps.searchResults.entries[this.searchId]
        ){
            this.setVideo(newProps.searchResults.entries[this.searchId]);
        }

    }

    loadVideo(props){
        var videoId = props.match.params.id;
       
        var query = SearchService.createQuery('solr');
        this.searchId = query.uuid;
        query.query = "*:*";
        query.filterQueries = ["content-type:/component/video", "objectId:" + videoId];

        this.props.search(query);
    }

    setVideo(searchResult) {
        var { setVideoInfo } = this.props;

        if(searchResult.numFound > 0){
            var video = searchResult.documents[0],
                categories = [],
                upcomingVideoHero = [];

            var videoStartDate = new Date(video.startDate_dt),
                now = new Date();

            if(video['content-type'] === '/component/stream' && videoStartDate > now ){
                //is an upcoming video, won't load player
                this.props.setHeaderGhost(true);

                upcomingVideoHero.push({
                    url: "#",
                    background: video.thumbnail,
                    title: video.title_s,
                    subtitle: video.description_html,
                    date: video.startDate_dt
                })
    
                // remove video info (if available)
                setVideoInfo(null);
                this.setState({ slider: upcomingVideoHero });
            }else{
                //is a video (regular video or stream) - will load player
                // remove upcoming stream slider info (if available)
                this.setState({ slider: null })
                this.props.setHeaderGhost(false);
                setVideoInfo(video);
            }

            //get categories for videoCategories component
            if( video["channels.item.key"].constructor === Array ){
                for (var i = 0, len = video["channels.item.key"].length; i < len; i++) {
                    categories.push( 
                        { key: video["channels.item.key"][i], value: video["channels.item.value_smv"][i] } 
                    );
                }
            }else{
                categories.push( 
                    { key: video["channels.item.key"], value: video["channels.item.value_smv"] } 
                );
            }

            this.setState({ categories: categories });
        }else{
            this.setState({
                notFound: true
            });
        }

    }

    showShareModal() {
        this.shareDialog.showModal();
    }

    renderDetailsSection( video ) {
        return(     
            <div>
                <div className="segment segment--dark">
                    <div className="video-details">
                        <div className="video-details__header">
                            <div className="video-details__titles">
                                {/* <div className="video-details__thumbnail">
                                    <div className="image">
                                        <div className="image__image">
                                        </div>
                                    </div>
                                </div> */}
                                <div className="video-details__titles-content">
                                    <h1 className="heading video-details__heading heading--smaller"> { video.title_s } </h1>
                                    <h1 className="heading video-details__episode heading--default"> { video.summary_s } </h1>
                                </div>
                            </div>
                            <div className="video-details__links">
                                <div className="inline-button inline-button__text video-details__links-link">
                                    <ModalDialog ref={(ref) => (this.shareDialog = ref)}>
                                        <input className="share-dialog__field" defaultValue={ window.location.href }/>
                
                                        <div className="share-dialog__social">
                                            {/* http://sharingbuttons.io/ */}

                                            {/* <!-- Sharingbutton Facebook --> */}
                                            <a className="resp-sharing-button__link" href={ `https://facebook.com/sharer/sharer.php?u=${ window.location.href }` } target="_blank" aria-label="Share on Facebook">
                                            <div className="resp-sharing-button resp-sharing-button--facebook resp-sharing-button--large"><div aria-hidden="true" className="resp-sharing-button__icon resp-sharing-button__icon--solid">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M18.77 7.46H14.5v-1.9c0-.9.6-1.1 1-1.1h3V.5h-4.33C10.24.5 9.5 3.44 9.5 5.32v2.15h-3v4h3v12h5v-12h3.85l.42-4z"/></svg>
                                                </div>Facebook</div>
                                            </a>

                                            {/* <!-- Sharingbutton Twitter --> */}
                                            <a className="resp-sharing-button__link" href={ `https://twitter.com/intent/tweet/?text=${ video.title_s }&amp;url=${ window.location.href }` } target="_blank" aria-label="Share on Twitter">
                                            <div className="resp-sharing-button resp-sharing-button--twitter resp-sharing-button--large"><div aria-hidden="true" className="resp-sharing-button__icon resp-sharing-button__icon--solid">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M23.44 4.83c-.8.37-1.5.38-2.22.02.93-.56.98-.96 1.32-2.02-.88.52-1.86.9-2.9 1.1-.82-.88-2-1.43-3.3-1.43-2.5 0-4.55 2.04-4.55 4.54 0 .36.03.7.1 1.04-3.77-.2-7.12-2-9.36-4.75-.4.67-.6 1.45-.6 2.3 0 1.56.8 2.95 2 3.77-.74-.03-1.44-.23-2.05-.57v.06c0 2.2 1.56 4.03 3.64 4.44-.67.2-1.37.2-2.06.08.58 1.8 2.26 3.12 4.25 3.16C5.78 18.1 3.37 18.74 1 18.46c2 1.3 4.4 2.04 6.97 2.04 8.35 0 12.92-6.92 12.92-12.93 0-.2 0-.4-.02-.6.9-.63 1.96-1.22 2.56-2.14z"/></svg>
                                                </div>Twitter</div>
                                            </a>

                                            {/* <!-- Sharingbutton Google+ --> */}
                                            <a className="resp-sharing-button__link" href={ `https://plus.google.com/share?url=${ window.location.href }` } target="_blank" aria-label="Share on Google+">
                                            <div className="resp-sharing-button resp-sharing-button--google resp-sharing-button--large"><div aria-hidden="true" className="resp-sharing-button__icon resp-sharing-button__icon--solid">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M11.37 12.93c-.73-.52-1.4-1.27-1.4-1.5 0-.43.03-.63.98-1.37 1.23-.97 1.9-2.23 1.9-3.57 0-1.22-.36-2.3-1-3.05h.5c.1 0 .2-.04.28-.1l1.36-.98c.16-.12.23-.34.17-.54-.07-.2-.25-.33-.46-.33H7.6c-.66 0-1.34.12-2 .35-2.23.76-3.78 2.66-3.78 4.6 0 2.76 2.13 4.85 5 4.9-.07.23-.1.45-.1.66 0 .43.1.83.33 1.22h-.08c-2.72 0-5.17 1.34-6.1 3.32-.25.52-.37 1.04-.37 1.56 0 .5.13.98.38 1.44.6 1.04 1.84 1.86 3.55 2.28.87.23 1.82.34 2.8.34.88 0 1.7-.1 2.5-.34 2.4-.7 3.97-2.48 3.97-4.54 0-1.97-.63-3.15-2.33-4.35zm-7.7 4.5c0-1.42 1.8-2.68 3.9-2.68h.05c.45 0 .9.07 1.3.2l.42.28c.96.66 1.6 1.1 1.77 1.8.05.16.07.33.07.5 0 1.8-1.33 2.7-3.96 2.7-1.98 0-3.54-1.23-3.54-2.8zM5.54 3.9c.33-.38.75-.58 1.23-.58h.05c1.35.05 2.64 1.55 2.88 3.35.14 1.02-.08 1.97-.6 2.55-.32.37-.74.56-1.23.56h-.03c-1.32-.04-2.63-1.6-2.87-3.4-.13-1 .08-1.92.58-2.5zM23.5 9.5h-3v-3h-2v3h-3v2h3v3h2v-3h3"/></svg>
                                                </div>Google+</div>
                                            </a>

                                            {/* <!-- Sharingbutton E-Mail --> */}
                                            <a className="resp-sharing-button__link" href={ `mailto:?subject=${ video.title_s }&amp;body=${ window.location.href }` } target="_self" aria-label="Share by E-Mail">
                                            <div className="resp-sharing-button resp-sharing-button--email resp-sharing-button--large"><div aria-hidden="true" className="resp-sharing-button__icon resp-sharing-button__icon--solid">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M22 4H2C.9 4 0 4.9 0 6v12c0 1.1.9 2 2 2h20c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zM7.25 14.43l-3.5 2c-.08.05-.17.07-.25.07-.17 0-.34-.1-.43-.25-.14-.24-.06-.55.18-.68l3.5-2c.24-.14.55-.06.68.18.14.24.06.55-.18.68zm4.75.07c-.1 0-.2-.03-.27-.08l-8.5-5.5c-.23-.15-.3-.46-.15-.7.15-.22.46-.3.7-.14L12 13.4l8.23-5.32c.23-.15.54-.08.7.15.14.23.07.54-.16.7l-8.5 5.5c-.08.04-.17.07-.27.07zm8.93 1.75c-.1.16-.26.25-.43.25-.08 0-.17-.02-.25-.07l-3.5-2c-.24-.13-.32-.44-.18-.68s.44-.32.68-.18l3.5 2c.24.13.32.44.18.68z"/></svg>
                                                </div>E-Mail</div>
                                            </a>
                                        </div>
                                        
                                    </ModalDialog>  

                                    <FontAwesomeIcon className="inline-button__icon" icon={ faShareAlt }/>
                                    <span className="inline-button__text" onClick={ () => this.showShareModal() }>share</span>
                                </div>
                                <a href="mailto:mail@mail.com" id="uservoice-video" className="video-details__links-link">
                                    <div className="inline-button inline-button__text"> 
                                        <FontAwesomeIcon className="inline-button__icon" icon={ faComment }/>
                                        <span className="inline-button__text">feedback</span>
                                    </div>
                                </a>
                            </div>
                        </div>

                        <div className="video-details__description">
                            <p>
                                { video.description_html }
                            </p>
                        </div>
                        <hr/>
                    </div>
                </div>
            </div>
        );
    }

    render() {
        var { videoInfo } = this.props;
        return (
            <div>
                { this.state.notFound && <NotFound/> }

                <VideoHolder>

                    { this.state && this.state.slider &&
                        <Slider data={ this.state.slider }
                            localData={ true }
                            hero={ true }
                        >
                        </Slider>
                    }

                    { videoInfo &&
                        this.renderDetailsSection( videoInfo )
                    }

                    { this.state && this.state.categories &&
                        <VideoCategories categories={ this.state.categories }
                                        exclude= { videoInfo }    
                        ></VideoCategories>
                    }
                    
                    {/* <VideoSidebar/>  */}
                </VideoHolder>
            </div>
            
        );
    }
}

function mapStateToProps(store) {
    return { 
        videoInfo: store.video.videoInfo,
        videoStatus: store.video.videoStatus,
        items: store.craftercms.items,
        searchResults: store.craftercms.search
    };
}

function mapDispatchToProps(dispatch) {
    return({
        getItem: (url) => { dispatch(getItem(url)) },
        search: (query) => { dispatch(search(query)) },
        setVideoStatus: (status) => { dispatch(setVideoStatus(status)) },
        setVideoInfo: (info) => { dispatch(setVideoInfo(info)) },
        setHeaderGhost: (ghost) => { dispatch(setHeaderGhost(ghost)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(Video);
