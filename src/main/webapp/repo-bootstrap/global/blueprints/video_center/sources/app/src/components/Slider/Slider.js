import React, { Component } from "react";
import { Carousel } from 'antd';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faAngleLeft, faAngleRight } from '@fortawesome/free-solid-svg-icons';

import { formatDate } from '../../utils';
import SliderHolder from './SliderStyle';

class Slider extends Component {
    componentDidMount() {
    
        if( this.props.localData ){
            this.setState({ slides: this.props.data });
        }else{
            this.props.getDescriptor(this.props.data.key);
        }
        
    }

    renderCountdown() {
        //since it is a hero (only one slide), it's the first item of array
        var { data } = this.props,
            formattedDate = formatDate(data[0].date);

        return (
            <div className="hero__countdown">
                <div className="countdown-container__content" id="countdown">
                    <div className="countdown--pre countdown--text">
                        <div className="countdown__label">Upcoming</div>
                        <div className="countdown__heading">{ formattedDate.month } { formattedDate.monthDay }</div>
                        <div className="countdown__live-time"> Live at { formattedDate.time } { formattedDate.timezone }</div>
                    </div>
                </div>                       
            </div>         
        );
    }

    renderSlides(slides) {
        return slides.map((slide, i) => {
            return (
                <div key={ i }>
                    <div className="discover-slider__inner">
                        <Link className="discover-slider__link" to={ slide.url }>
                            <div>
                                <div className="image discover-slider__inner--background discover-slider__inner--background-mobile">
                                    <div className="image__image" 
                                        style={{ backgroundImage: `url(${ slide.background })` }}>
                                    </div>
                                </div>
                                <div className="image discover-slider__inner--background discover-slider__inner--background-desktop">
                                    <div className="image__image" 
                                        style={{ backgroundImage: `url(${ slide.background })` }}>
                                    </div>
                                </div>
                            </div>
                            <div className={"discover-slider__inner--content" + (this.props.hero ? ' hero_content' : '') }>
                                { slide.vod &&
                                    <div className="discover-slider__inner--vod">
                                        <span className="discover-slider__inner--vod-label">
                                            { slide.vod }
                                        </span>
                                    </div>
                                }
                                
                                <h1 className="heading discover-slider__inner--title heading--medium heading--slider">
                                    { slide.title }
                                </h1>

                                { slide.logo &&
                                    <div className="discover-slider__inner--title discover-slider__inner--title--logo" 
                                        style={{ backgroundImage: `url(${ slide.logo })` }}>
                                    </div>
                                }

                                <div className="discover-slider__inner--subtitle">
                                    { slide.subtitle }
                                </div>

                                { this.props.hero &&
                                    this.renderCountdown()
                                }

                            </div>
                        </Link>
                    </div>
                </div>
            );
        });
    }

    changeSlide( direction ) {

        if("previous" === direction){
            this.slider.prev()
        }else{
            this.slider.next()
        }
    }

    renderSliderControls(){
        return(
            <div className="discover-slider__inner--nav">
                <label className="discover-slider__inner--nav-button discover-slider__inner--nav-prev" onClick={() => this.changeSlide("previous")}>
                    <FontAwesomeIcon className="nav-icon" icon={ faAngleLeft }/>
                </label>
                <label className="discover-slider__inner--nav-button discover-slider__inner--nav-next" onClick={() => this.changeSlide("next")}>
                    <FontAwesomeIcon className="nav-icon" icon={ faAngleRight }/>
                </label>
            </div> 
        );
    }

    renderSliderLocalData() {
        return (
            <SliderHolder className="hero-container hero-container__ghost">
                <Carousel className="discover-slider" 
                    effect="fade"
                    ref={node => (this.slider = node)}
                    autoplay>
                    { this.renderSlides(this.state.slides) }
                </Carousel> 

                {this.state && this.state.slides && this.state.slides.length > 1 &&
                    this.renderSliderControls()
                }   
            </SliderHolder>
        )
    }

    renderSliderDescriptor(descriptor) {
        var slides = descriptor.component.slides;

        if( !(slides.item instanceof Array) ){
            slides = [slides.item];
        }else{
            slides = slides.item;
        }

        return (
            <SliderHolder className="hero-container hero-container__ghost">
                <Carousel className="discover-slider" 
                    effect="fade"
                    ref={node => (this.slider = node)}
                    autoplay>
                    { this.renderSlides(slides) }
                </Carousel> 

                { slides.length > 1 &&
                    this.renderSliderControls()
                }   
            </SliderHolder>
        )
    }

    render() {
        return (
            <div>
                { this.props.descriptors && this.props.descriptors[this.props.data.key] &&
                    this.renderSliderDescriptor(this.props.descriptors[this.props.data.key])
                }

                { this.state && this.state.slides &&
                    this.renderSliderLocalData()
                }
            </div>
        );
    }
}

export default Slider;