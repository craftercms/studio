import React, { Component } from "react";
import { connect } from 'react-redux';

import NotFound from './404';
import { setHeaderGhost } from '../../actions/headerActions';

class ErrorPage extends Component {
    componentDidMount() {
        this.props.setHeaderGhost(true);
    }
    componentWillUnmount() {
        this.props.setHeaderGhost(false);
    }

    render() {
        return (
            <div>
                <NotFound/>
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
        setHeaderGhost: (ghost) => { dispatch(setHeaderGhost(ghost)) }
    })
}

export default connect(mapStateToProps, mapDispatchToProps)(ErrorPage);