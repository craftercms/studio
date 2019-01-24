import React, { Component } from "react";
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlay } from '@fortawesome/free-solid-svg-icons';
import { isNullOrUndefined } from 'util'

import { crafterConf } from '@craftercms/classes';
import { SearchService } from '@craftercms/search';

import { formatDate } from '../../utils';

class Cards extends Component {
    componentDidMount() {
        this.searchCards(this.props);
    }

    componentWillReceiveProps(newProps) {
        this.searchCards(newProps);
    }

    searchCards(props) {
        const self = this;

        var query = SearchService.createQuery('solr'),
            category = props.category;

        if(category.query){
            query.query = "*:*";
            query.filterQueries = category.query;

            if( !isNullOrUndefined(category.numResults) ){
                query.numResults = category.numResults;
            }

            if( !isNullOrUndefined(category.sort) ){
                query.addParam('sort', category.sort);
            }
        }else{
            category = props.category.key
            query.query = "*:*";
            // query.filterQueries = props.exclude
            //     ? ["content-type:/component/video", "channels.item.key:" + category, '-id: "' + props.exclude + '"']
            //     : ["content-type:/component/video", "channels.item.key:" + category];

                query.filterQueries = ["content-type:/component/video", "channels.item.key:" + category];
        }

        SearchService
            .search(query, crafterConf.getConfig())
            .subscribe(cards => {
                self.setState({ cards: cards.response.documents });
            });
    }

    renderCards() {
        return this.state.cards.map((card, i) => {
            var componentUrl = card["content-type"] === "/component/stream" ? "/stream/" : "/video/",
                categoryType = this.props.category.type ? this.props.category.type : "video-card",
                videoName;

            switch( categoryType ) {
                case "video-card":
                    videoName = card.title_s ? (card.title_s).toLowerCase().replace(/ /g, '-') : '';
                    videoName = encodeURI(videoName);

                    if(card.startDate_dt) {
                        var videoStartDate = new Date(card.startDate_dt),
                            now = new Date(),
                            formattedDate = formatDate(card.startDate_dt);
                    }

                    return (
                        <div className="static-grid__item" key={ card.id }>
                            <div className="video-card video-card--has-description">
                                <Link className="video-card__link" to={ `${componentUrl}${card.objectId}` }>
                                    <div>
                                        <div className="image video-card__image--background" style={{ background: 'transparent' }}>
                                            <div className="image__image" style={{ backgroundImage: `url(${ card.thumbnail })` }}></div>
                                        </div>
                                        <video className="image preview-video" loop="" preload="auto" playsInline=""></video>
                                    </div>
                                    { videoStartDate > now &&
                                        <div className="video-card__date-info">
                                            <div className="day">
                                                { formattedDate.month } { formattedDate.monthDay }
                                            </div>
                                            <div className="time">
                                                { formattedDate.weekDay } @ { formattedDate.time } { formattedDate.timezone }
                                            </div>
                                        </div>
                                    }
                                    <div className="video-card__content">
                                        <div className="video-card__time"> { card.length } </div>
                                        <h3 className="heading video-card__heading heading--default heading--card">{ card.title_s }</h3>
                                        <div className="video-card__description"> { card.summary_s } </div>
                                        <div className="video-card__long-description"> { card.description_html } </div>
                                        <div className="video-card__progress" style={{ width: '0%' }}></div>
                                    </div>
                                    <div className="video-card__play-button">
                                        <FontAwesomeIcon className="play-icon" icon={ faPlay }/>
                                    </div>
                                </Link>
                            </div>
                        </div>
                    );
                case "channel-card-alt":
                    var url = card["file-name"].replace(".xml", "");

                    return (
                        <div className="static-grid__item" key={card.id}>
                            <div className="channel-card-alt">
                                <Link className="channel-card-alt__link" to={`/channel/${ url }`}>
                                    <div className="image channel-card-alt__image">
                                        <div className="image__image" style={{ backgroundImage: `url(${ card.thumbnailImage })` }}>
                                            <div className="channel-card-alt__overlay"></div>
                                        </div>
                                    </div>
                                    <h2 className="channel-card-alt__heading"> { card["internal-name"] } </h2>
                                </Link>
                            </div>
                        </div>
                    );
                case "standard-card":
                    return (
                        <div className="static-grid__item" key={card.id}>
                            {/* <div className="standard-card">
                                <a className="standard-card__link" href="/show/AP-1V6V7K8HN1W11/the-way-of-the-wildcard">
                                    <div className="image standard-card__image">
                                        <div className="image__image" style={{ backgroundImage: `url(${ article.image })` }}></div>
                                    </div>
                                </a>
                            </div> */}
                        </div>
                    );
                case "live-event-item":
                    var dateFormatted = formatDate(card.startDate_dt);

                    videoName = card.title_s ? (card.title_s).toLowerCase().replace(/ /g, '-') : '';
                    videoName = encodeURI(videoName);

                    return (
                        <div className="live-events-item" key={ card.id }>
                            <Link className="live-events-item__link" to={`/stream/${ card.objectId }/${ videoName }`}>
                                <div className="live-events-item__image">
                                    <div className="live-events-item__background">
                                        <div className="image">
                                            <div className="image__image" style={{ backgroundImage: `url("${ card.thumbnail }")` }}></div>
                                        </div>
                                    </div>
                                </div>
                                <div className="live-events-item__content">
                                    <div className="live-events-item__date">
                                        <h2 className="heading heading--default">{dateFormatted.month} {dateFormatted.monthDay}</h2>
                                    </div>
                                    <div className="live-events-item__time">{ dateFormatted.weekDay } @ { dateFormatted.time } { dateFormatted.timezone }</div>
                                    <div className="live-events-item__detail">
                                        <div className="live-events-item__heading-group">
                                            <h3 className="live-events-item__heading">{ card.title_s }</h3>
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        </div>
                    );
                default:
                    return (
                        <div></div>
                    );
            }
        });
    }

    render() {
        return (
            <div className={ this.props.category.type !== "live-event-item" ? "static-grid__items" : "" } >
                { this.state && this.state.cards &&
                    this.renderCards()
                }
                { this.state && this.state.cards && this.state.cards.length === 0 &&
                    <div className="segment">
                        <div style={{textAlign: "center", fontSize: "3rem", fontWeight: '700', padding: "15rem 0px 25rem", minHeight: "50vh" }}>
                            No results
                        </div>
                    </div>
                }
            </div>
        );
    }
}

export default Cards;
