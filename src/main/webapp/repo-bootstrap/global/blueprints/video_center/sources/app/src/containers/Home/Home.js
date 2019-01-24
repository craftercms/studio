import React, { Component } from 'react';
import { connect } from 'react-redux';
import { isNullOrUndefined } from 'util';
import { getDescriptor } from "@craftercms/redux";

import { setVideoDocked } from '../../actions/videoPlayerActions';
import { setHeaderGhost } from '../../actions/headerActions';
import Slider from '../../components/Slider/Slider.js';
import VideoCategories from '../../components/VideoCategories/VideoCategories.js';

class Home extends Component {
    componentWillMount() {
        this.props.setVideoDocked( false );

        this.descriptorUrl = '/site/website/index.xml';

        if(isNullOrUndefined(this.props.descriptors[this.descriptorUrl])){
            this.props.getDescriptor(this.descriptorUrl);
        }
    }

    componentDidMount() {
        this.props.setHeaderGhost(true);
    }
    componentWillUnmount() {
        this.props.setHeaderGhost(false);
    }

    renderSlider(descriptor) {
        if( descriptor.page.slider.item ) {
            return (
                <Slider data={ descriptor.page.slider.item }
                        getDescriptor={ this.props.getDescriptor }
                        descriptors={ this.props.descriptors }>
                </Slider>
            )
        }
    }

    renderHomeContent(descriptor) {
        var page = descriptor.page,
            categories = [
            {
                key: "featured-videos",
                value: "Featured Videos",
                query: ['content-type:"/component/video"', 'featured: "true"'],
                numResults: page.maxVideosDisplay
            },
            { 
                key: "latest-videos", 
                value: "Latest Videos",
                query: ['content-type:"/component/video"'],
                sort: "createdDate_dt desc",
                numResults: page.maxVideosDisplay
            },
            {
                key: "featured-channels",
                value: "Featured Channels",
                type: "channel-card-alt",
                query: ['content-type:"/component/component-channel"', 'featured: "true"'],
                numResults: page.maxChannelsDisplay
            }
        ];

        return (
            <div>
                { this.renderSlider(descriptor) }
                
                <VideoCategories categories={ categories }>
                </VideoCategories>
            </div>
        );
    }

    render() {
        var { descriptors } = this.props;

        return (
            <div>
                { descriptors && descriptors[this.descriptorUrl] &&
                    this.renderHomeContent(descriptors[this.descriptorUrl])
                }   
            </div>
        );
    }
}

function mapStateToProps(store) {
    return { 
        videoStatus: store.video.videoStatus,
        descriptors: store.craftercms.descriptors.entries
    };
}

function mapDispatchToProps(dispatch) {
    return({
        setVideoDocked: (docked) => { dispatch(setVideoDocked(docked)) },
        getDescriptor: (url) => { dispatch(getDescriptor(url)) },
        setHeaderGhost: (ghost) => { dispatch(setHeaderGhost(ghost)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(Home);
