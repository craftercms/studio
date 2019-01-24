import React, { Component } from "react";
import { connect } from "react-redux";

import { setVideoDocked } from "../../actions/videoPlayerActions";
import VideoCategories from '../../components/VideoCategories/VideoCategories.js';

class Channel extends Component {
    componentWillMount() {
        this.props.setVideoDocked( false );
    }

    componentDidMount(){
        this.getInfo(this.props);
    }

    componentWillReceiveProps(newProps){
        if(this.props.match.params.id !== newProps.match.params.id){
            this.getInfo(newProps);
        }
    }

    getInfo(props) {
        var params = props.match.params,
            category = {
                key: '',
                value: params.categoryName,
                viewAll: false,
                numResults: 1000
            },
            query = params.query.replace(/_/g, '/').split(',');

        if(query[0].indexOf(":") === -1 && query.length === 1){
            category.key = query[0];
        }else{
            category.query = query;
        }
        
        if(params.sort){
            category.sort = params.sort;
        }

        this.setState({
            categories: [
                category
            ]
        })
    }

    render() {
        return (
            <div>
                { this.state && this.state.categories &&
                    <VideoCategories 
                        categories={ this.state.categories } >
                    </VideoCategories>
                }
            </div>
        );
    }
}

function mapStateToProps(store) {
    return { 
    };
}

function mapDispatchToProps(dispatch) {
    return({
        setVideoDocked: (docked) => { dispatch(setVideoDocked(docked)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(Channel);
