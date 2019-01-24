import React, { Component } from "react";
import { Switch, Route, withRouter } from 'react-router-dom';
import { connect } from "react-redux";
import { getNav } from '@craftercms/redux';

import Home from './containers/Home/Home.js';
import Video from './containers/Video/Video.js';
import Channels from './containers/Channels/Channels.js';
import Channel from './containers/Channel/Channel.js';
import LiveEvents from './containers/LiveEvents/LiveEvents.js';
import Search from './containers/Search/Search.js';
import List from "./containers/List/List.js";
import ErrorPage from './containers/Errors/errorPage';

// The Main component renders one of the provided Routes 
class Router extends Component {
    componentWillMount() {
        //Need to locally set components in order to dinamically load them in router
        this.Channels = Channels;
        this.LiveEvents = LiveEvents;

        this.props.getNav('/site/website');
    }

    renderRouteEntries() {
        var rootId = '/',
            me = this;

        return this.props.nav.childIds[rootId].map((id, i) =>{
            var navItem = this.props.nav.entries[id];

            return (
                <Route key={ i }  exact path={navItem.url} component={ me[navItem.attributes.reactComponent] }/>
            );
        });
    }

    render() {
        const { nav } = this.props;

        return (
        <Switch>
            <Route exact path='/' component={Home}/>
            <Route exact path='/video/:id/:videoName?' component={Video}/>
            <Route exact path='/stream/:id/:videoName?' component={Video}/>
            <Route exact path='/search' component={Search}/>
            <Route exact path='/search/:query' component={Search}/>
            <Route exact path='/channel/:name' component={Channel}/>
            <Route exact path='/list/:id' component={List}/>
            <Route exact path='/list/:categoryName/:query/:sort?' component={List}/>
            { nav && nav.entries['/'] &&
                this.renderRouteEntries()
            }
            <Route component={ErrorPage} />
        </Switch>
        );
    }
}

function mapStateToProps(store) {
    return { nav: store.craftercms.navigation };
}

function mapDispatchToProps(dispatch) {
    return({
        getNav: (url) => { dispatch(getNav(url)) }
    })
}

export default withRouter(connect(
    mapStateToProps,
    mapDispatchToProps
)(Router));