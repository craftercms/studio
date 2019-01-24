import React, { Component } from "react";
import { connect } from "react-redux";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch } from '@fortawesome/free-solid-svg-icons';

import SearchHolder from './SearchStyle';
import VideoCategories from '../../components/VideoCategories/VideoCategories.js';
import { setVideoDocked } from "../../actions/videoPlayerActions";

const WAIT_INTERVAL = 1000;
class Search extends Component {

    constructor(props) {
        super(props);

        this.searchId = this.props.match.params.query

        this.state = {
            categories: this.setCategories(this.searchId)
        };
    
    }

    componentWillMount() {
        this.props.setVideoDocked( false );
        this.timer = null;
    }

    componentDidMount() {
        this.appContentEl = document.getElementById("app-content");
        this.appContentEl.classList.add("search-content");
    }

    componentWillUnmount() {
        this.appContentEl.classList.remove("search-content");
    }

    setCategories(searchId){
        return [{ 
                key: "top-results", 
                value: "Top Results", 
                query: ["content-type:/component/video", "title_t: (*" + searchId + "*)"],
                viewAll: false,
                numResults: 90
            }];
    }

    onChange( event ) {
        var me = this,
            value = event.target.value;

        clearTimeout(this.timer);
        this.timer = setTimeout(function() {
            var newCategories = me.setCategories(value);
        
            me.setState({categories: newCategories });      
        }, WAIT_INTERVAL);

    }

    render() {
        return (
            <SearchHolder>
                <div className="search-bar--sticky">
                    <div className="search-bar search-bar--visible">
                        <div className="search-bar__container">
                            <div className="search-bar__inner">
                                <div className="search-bar__icon">
                                    <FontAwesomeIcon className="search__icon" icon={ faSearch }/>
                                </div>
                                <input type="text" className="search-bar__input" placeholder="Start Typing..." 
                                    defaultValue={ this.searchId }
                                    onChange={this.onChange.bind(this)}/>
                            </div>
                        </div>
                    </div>
                </div>

                <VideoCategories 
                    categories={ this.state.categories }>
                </VideoCategories>
            </SearchHolder>
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

export default connect(mapStateToProps, mapDispatchToProps)(Search);
