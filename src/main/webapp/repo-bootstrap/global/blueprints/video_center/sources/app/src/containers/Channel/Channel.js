import React, { Component } from "react";
import { connect } from "react-redux";
import { isNullOrUndefined } from 'util';
import { getDescriptor } from "@craftercms/redux";

import { setVideoDocked } from "../../actions/videoPlayerActions";
import { setHeaderGhost } from '../../actions/headerActions';
import Slider from '../../components/Slider/Slider.js';
import VideoCategories from '../../components/VideoCategories/VideoCategories.js';
import NotFound from '../Errors/404';

class Channel extends Component {
    constructor(props) {
        super(props);

        //categories = new/featured, all videos, all streams, related

        this.getChannelInfo(props);
    }

    componentWillMount() {
        this.props.setVideoDocked( false );
    }

    componentDidMount() {
        this.props.setHeaderGhost(true);
    }

    componentWillUnmount() {
        this.props.setHeaderGhost(false);
    }

    componentWillReceiveProps(newProps){
        if(this.props.match.url !== newProps.match.url){
            this.getChannelInfo(newProps);
        }
    }

    getChannelInfo(props) {
        var channelName = props.match.params.name;

        this.descriptorUrl = `/site/components/channel/${ channelName }.xml`;

        if(isNullOrUndefined(this.props.descriptors[this.descriptorUrl])){
            this.props.getDescriptor(this.descriptorUrl);
        }
    }

    renderChannelContent(descriptor) {
        var component = descriptor.component,
            channelHero = [],
            channelContent = descriptor.component,
            channelTags = channelContent.tags.item,
            tagsFilter = 'tags.item.value: ',
            categories;

        channelHero.push({
            url: "#",
            background: channelContent.heroImage,
            title: channelContent['internal-name'],
            subtitle: channelContent.description
        });

        //get channel tags
        for(var x = 0; x < channelTags.length; x++) {
            var tag = channelTags[x];

            tagsFilter += '"' + tag.value + '"';
            tagsFilter += x < channelTags.length - 1 ? ' OR ' : '';
        }

        categories = [
            {
                key: "featured-videos",
                value: "Featured Videos",
                query: ["content-type:/component/video", 'channels.item.key: "' + channelContent.channelKey + '"'],
                numResults: component.maxVideosDisplay
            },
            {
                key: "related-channels",
                value: "Related Channels",
                type: "channel-card-alt",   //TO RENDER CHANNEL CARD STYLING
                query: ['content-type:"/component/component-channel"', tagsFilter, '-file-name: "' + channelContent['file-name'] + '"'],
                numResults: component.maxChannelsDisplay
            }
        ];

        return (
            <div>
                <Slider data={ channelHero }
                    localData={ true }
                >
                </Slider>
                <VideoCategories
                    categories={ categories } >
                </VideoCategories>
            </div>
        );
    }

    render() {
        const { descriptors, descriptorsLoading } = this.props;

        if( (descriptorsLoading[this.descriptorUrl] === false ) && isNullOrUndefined(descriptors[this.descriptorUrl]) ){
            return (
                <NotFound/>
            )
        }else{
            return (
                <div>
                    { descriptors && descriptors[this.descriptorUrl] &&
                        this.renderChannelContent(descriptors[this.descriptorUrl])
                    }
                </div>
            );
        }
    }
}

function mapStateToProps(store) {
    return {
        videoInfo: store.video.videoInfo,
        videoStatus: store.video.videoStatus,
        descriptors: store.craftercms.descriptors.entries,
        descriptorsLoading: store.craftercms.descriptors.loading
    };
}

function mapDispatchToProps(dispatch) {
    return({
        setVideoDocked: (docked) => { dispatch(setVideoDocked(docked)) },
        getDescriptor: (url) => { dispatch(getDescriptor(url)) },
        setHeaderGhost: (ghost) => { dispatch(setHeaderGhost(ghost)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(Channel);
