import React, { Component } from "react";
import ReactDOM from "react-dom";

import SidebarHolder from "./SidebarStyle";

class VideoSidebar extends Component {
    constructor(props){
        super(props);
        this.element = document.createElement("div");
        this.element.className = "app-content__sidebar app-content__sidebar--animate app-content__sidebar--visible";
    }

    componentWillMount() {
        
    }

    componentWillUnmount() {
        this.sidebarContainer.removeChild(this.element);
    }

    componentDidMount() {
        this.sidebarContainer = document.getElementById("app-content");
        this.sidebarContainer.appendChild(this.element);
    }

    render() {
        return ReactDOM.createPortal(this._renderSidebar(), this.element)
    }

    _renderSidebar() {
        return (
            <SidebarHolder>
                <div className="video-sidebar">
                    <ul className="video-sidebar__tabs">
                        <li className="video-sidebar__tab video-sidebar__tab--active">
                            <div className="video-sidebar__tab-name">Film Playlist</div>
                        </li>
                    </ul>

                    <div className="video-sidebar__content video-sidebar__content--active">
                        <div className="video-sidebar__content-scroll">
                            <div className="video-sidebar__content-inner--custom">
                                <div className="playlist">
                                    <a className="playlist__item playlist__item--playable" href="/video/AP-1QBDNKH9W1W11/andy-noakley-profile?playlist=AP-1PFA8VUKN1W11">
                                        <div className="playlist__item--container">
                                            <div className="playlist__image">
                                                <div className="image">
                                                    <div className="image__image" style={{ backgroundImage: `url("/static-assets/images/videos/6f1b063a-cb5d-6afd-ce79-02972ba88fb9/auditorium-benches-chairs-207691.jpg")` }}></div>
                                                </div>
                                                <div className="playlist__progress">
                                                    <div className="playlist__progress-inner"></div>
                                                </div>
                                            </div>
                                            <div className="playlist__status">
                                                <span className="playlist__duration">4 min</span>
                                            </div>
                                            <div className="playlist__title">Andy Noakley Profile</div>
                                            <div className="playlist__subtitle">Parallel Universe: BTS</div>
                                        </div>
                                    </a>
                                    <a className="playlist__item playlist__item--playable" href="/video/AP-1QBDNKH9W1W11/andy-noakley-profile?playlist=AP-1PFA8VUKN1W11">
                                        <div className="playlist__item--container">
                                            <div className="playlist__image">
                                                <div className="image">
                                                    <div className="image__image" style={{ backgroundImage: `url("/static-assets/images/videos/6f1b063a-cb5d-6afd-ce79-02972ba88fb9/auditorium-benches-chairs-207691.jpg")` }}></div>
                                                </div>
                                                <div className="playlist__progress">
                                                    <div className="playlist__progress-inner"></div>
                                                </div>
                                            </div>
                                            <div className="playlist__status">
                                                <span className="playlist__duration">4 min</span>
                                            </div>
                                            <div className="playlist__title">Andy Noakley Profile</div>
                                            <div className="playlist__subtitle">Parallel Universe: BTS</div>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </SidebarHolder>

                // <div className="video-sidebar__toggle">

                // </div>
        );
    }
}

export default VideoSidebar;