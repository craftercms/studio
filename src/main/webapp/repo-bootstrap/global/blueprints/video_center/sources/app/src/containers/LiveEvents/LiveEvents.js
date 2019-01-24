import React, { Component } from "react";
import { connect } from "react-redux";

import VideoCategories from '../../components/VideoCategories/VideoCategories.js';
import { setVideoDocked } from "../../actions/videoPlayerActions";

class LiveEvents extends Component {
    constructor(props) {
        super(props);
        this.state = {
            searchId: this.props.match.params.query,
            categories: [
                {
                    key: "upcoming-events",
                    value: "Upcoming Events",
                    type: "live-event-item",
                    query: ['content-type:"/component/stream"', 'startDate_dt:[NOW TO *]'],
                    numResults: 6
                },
                { 
                    key: "past-events", 
                    value: "Past Events",
                    type: "live-event-item",
                    query: ['content-type:"/component/stream"', 'endDate_dt:[* TO NOW]'] ,
                    numResults: 6
                }
            ]
        };
    }

    componentWillMount() {
        this.props.setVideoDocked( false );
    }
    render() {
        return (
        <div>
            {/* <Slider></Slider> */}

            {/* <VideoCategories></VideoCategories> */}

            <VideoCategories 
                categories={ this.state.categories }>
            </VideoCategories>
        </div>
        );
    }
}

function mapStateToProps(store) {
    return { 
        videoInfo: store.video.videoInfo,
        videoStatus: store.video.videoStatus
    };
}

function mapDispatchToProps(dispatch) {
    return({
        setVideoDocked: (docked) => { dispatch(setVideoDocked(docked)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(LiveEvents);