import React, { Component } from "react";
import Scrollspy from 'react-scrollspy';
import { Link } from 'react-router-dom';
import { isNullOrUndefined } from 'util';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faAngleRight } from '@fortawesome/free-solid-svg-icons';

import VideoCategoriesHolder from "./VideoCategoriesStyle";
import Cards from '../Cards/Cards.js';

class VideoCategories extends Component {
    constructor(props) {
        super(props);

        this.sectionsScrollSpy = [];

        props.categories.forEach((category, i) => {
            this.sectionsScrollSpy.push("section-" + category.key);
        });
    }

    renderCards(category) {
        const isSearch = this.props.query && category.key === "top-results";
        
        if ( isSearch ) {
            return <Cards category={category} query={this.props.query}></Cards>;
        }else{
            if(this.props.exclude){
                return <Cards category={category} exclude={ this.props.exclude }></Cards>;
            }else{
                return <Cards category={category}></Cards>;
            }
        }
    }

    renderArticlesSections(){
        return this.props.categories.map((category, i) => {
            var gridElClass,
                categoryType = category.type ? category.type : "video-card",
                showViewAll = isNullOrUndefined(category.viewAll) ? true : category.viewAll

            switch( categoryType ) {
                case "video-card":
                    gridElClass = "static-grid static-grid--3 static-grid--break-at-480";
                    break;
                case "channel-card-alt":
                    gridElClass = "static-grid static-grid--3 static-grid--break-at-480";   
                    // gridElClass = "static-grid static-grid--4 static-grid--standard static-grid--break-at-480";
                    break;
                case "standard-card":
                    gridElClass = "static-grid static-grid--4 static-grid--standard static-grid--break-at-480";
                    break;
                default:
                    gridElClass = "";
            }

            if( categoryType === "video-card"){
                var categoryName = encodeURI(category.value),
                    sort = category.sort ? encodeURI(category.sort) : null,
                    query = category.query ? encodeURI(category.query.toString()) : category.key,
                    viewAllURL;

                query = query.replace(/\//g, '_');

                viewAllURL = `/list/${ categoryName }/${ query }`;
                viewAllURL = sort ? `${ viewAllURL }/${ sort }` : viewAllURL;
            }

            return (
                <section className="segment" key={i} id={"section-" + category.key}>
                    <div className="content-container__block content-container__block--0 content-container__block--active">
                        <div className="segment">
                            <h2 className="heading heading--default heading--section" 
                                style={ (showViewAll) ? { display: 'inline-block' } : { display: 'none' } }>
                                { category.value }
                            </h2>
                        
                            { categoryType === "video-card" && showViewAll &&
                                <Link className="collection__item--link" to={ viewAllURL }>
                                    <span>
                                        <FontAwesomeIcon className="icon" icon={ faAngleRight }/>
                                        View All
                                    </span>
                                </Link>
                            }

                            <div className={ gridElClass }>
                                {this.renderCards( category )}
                            </div>
                        </div>
                    </div>
                </section>
            );
        });
    }

    renderCategoriesItems() {
        
        return this.props.categories.map((category, i) => {
            return (
                <li key={i} className={ "inline-nav__item inline-nav__item_0" } >
                    <a href={"#section-" + category.key} className={ "inline-nav__link" }>
                        { category.value }
                    </a>
                </li>
            );
        });
    }

    handleScroll() {
        var stickyBar = document.getElementById('stickyBar');
        var stickyBarTop = stickyBar.getBoundingClientRect().top;

        if( stickyBarTop === 0 ){
            stickyBar.classList.add("inline-nav__sticky--stuck");
        }else{
            stickyBar.classList.remove("inline-nav__sticky--stuck");
        }
    }

    //horizontal scroll to category (when mobile or many categories that overflow the bar space)
    scrollMenuToCategory(sectionEl) {
        if(sectionEl){
            var menuEl = document.querySelector("[href='#" + sectionEl.id + "']"),
                menuContainer = document.querySelector("#stickyBar .inline-nav__ul"),
                scrollTo = menuEl.offsetLeft - 40;      // -40 because of left padding

            menuContainer.scrollLeft = scrollTo;
        }
    }

    componentDidMount() {
        document.getElementsByClassName("app-content__main")[0].addEventListener('scroll', this.handleScroll);
    }

    componentWillUnmount() {
        document.getElementsByClassName("app-content__main")[0].removeEventListener('scroll', this.handleScroll);
    }

    render() {
      
        return (
            <VideoCategoriesHolder>
                
                <div id="stickyBar" className="inline-nav__sticky">
                    <nav className="inline-nav inline-nav--align-left">
                        <div className="inline-nav__inner">
                            <Scrollspy className="inline-nav__ul" 
                                       items={ this.sectionsScrollSpy } 
                                       currentClassName="inline-nav__item--active"
                                       rootEl={ ".app-content__main" }
                                       ref={node => (this.scrollspy = node)}
                                       onUpdate={ el => { this.scrollMenuToCategory(el) } }>
                                {this.renderCategoriesItems()}
                            </Scrollspy>
                        </div>
                    </nav>
                </div>

                <div className="content-container">
                    { this.renderArticlesSections() }
                </div>

            </VideoCategoriesHolder>
        );
    }
}

export default VideoCategories;