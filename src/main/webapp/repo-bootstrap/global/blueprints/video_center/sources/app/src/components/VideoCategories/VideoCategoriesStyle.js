import styled from 'styled-components';
import { palette } from 'styled-theme';

const VideoCategoriesHolder = styled.div`
    
    .inline-nav__sticky{
        position: -webkit-sticky;
        position: sticky;
        top: 0;
        z-index: 996;

        &.inline-nav__sticky--stuck{
            .inline-nav{
                -webkit-transition: background-color .2s cubic-bezier(0,1,.75,1);
                transition: background-color .2s cubic-bezier(0,1,.75,1);
                background-color: ${ palette('primary', 1) };
                -webkit-box-shadow: 0 0 20px ${ palette('primary', 2) };
                box-shadow: 0 0 20px ${ palette('primary', 2) };

                .inline-nav__inner{
                    &:before, :after {
                        width: 6.5rem;
                    }

                    &:before {
                        background: -webkit-gradient(linear,left top,right top,color-stop(35%,#23252a),color-stop(75%,rgba(35,37,42,.5)),to(rgba(35,37,42,0)));
                        background: linear-gradient(90deg,#23252a 35%,rgba(35,37,42,.5) 75%,rgba(35,37,42,0));
                    }

                    &:after {
                        right: 0;
                        background: -webkit-gradient(linear,right top,left top,color-stop(35%,#23252a),color-stop(90%,rgba(35,37,42,.5)),to(rgba(35,37,42,0)));
                        background: linear-gradient(270deg,#23252a 35%,rgba(35,37,42,.5) 90%,rgba(35,37,42,0));
                    }

                    .inline-nav__ul{
                        .inline-nav__link {
                            -webkit-transition: padding .2s cubic-bezier(0,1,.75,1),color .4s cubic-bezier(0,1,.75,1),border-color .4s cubic-bezier(0,1,.75,1);
                            transition: padding .2s cubic-bezier(0,1,.75,1),color .4s cubic-bezier(0,1,.75,1),border-color .4s cubic-bezier(0,1,.75,1);
                            padding-bottom: 2.1rem !important;
                        }
                    }
                }
            }   
        }        

        .inline-nav {
            background: ${ palette('primary', 0) };
            height: 6.8rem;
            position: relative;
            z-index: 996;
            text-transform: uppercase;
            font-weight: 500;
            font-size: 1.3rem;
            overflow: hidden;

            &:before {
                z-index: 996;
                pointer-events: none;
                content: "";
                top: 0;
                left: 0;
                position: absolute;
                height: 100%;
                width: 100%;
            }

            @media (min-width: 28.125em){
                font-size: 1.4rem;
                letter-spacing: 0;
            }
            
            @media (min-width: 48em){
                font-size: 1.6rem;
            }

            &.inline-nav--align-left {
                text-align: left;
            }
        
            .inline-nav__inner {
                position: relative;
                margin: 0 auto;
                min-height: .1rem;
                width: 130rem;
                max-width: 100%;

                & ::-webkit-scrollbar {
                    display: none;
                }

                @media (min-width: 75em){
                    width: 115rem;
                }

                @media (min-width: 87.5em){
                    width: 125rem;
                }

                @media (min-width: 114.0625em){
                    width: 140rem;
                }

                &:after, :before {
                    content: "";
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    z-index: 996;
                    -webkit-transition: background transition-curve 1s;
                    transition: background transition-curve 1s;
                }                

                &:before {
                    width: 6.5rem;
                    background: -webkit-gradient(linear,left top,right top,color-stop(35%,#141519),color-stop(75%,rgba(20,21,25,.5)),to(rgba(20,21,25,0)));
                    background: linear-gradient(90deg,#141519 35%,rgba(20,21,25,.5) 75%,rgba(20,21,25,0));
                }

                &:after {
                    right: 0;
                    width: 6.5rem;
                    background: -webkit-gradient(linear,right top,left top,color-stop(35%,#141519),color-stop(90%,rgba(20,21,25,.5)),to(rgba(20,21,25,0)));
                    background: linear-gradient(270deg,#141519 35%,rgba(20,21,25,.5) 90%,rgba(20,21,25,0));
                }
    
                .inline-nav__ul {
                    margin: 0;
                    padding: 0;
                    border: 0;
                    vertical-align: baseline;
                    font-family: inherit;
                    font-size: inherit;
                    font-weight: inherit;
                    line-height: inherit;
                    width: auto;
                    list-style: none;
                    margin: 0 auto;
                    min-height: .1rem;
                    width: 110rem;
                    max-width: 100%;
                    padding: 0 4rem;
                    position: relative;
                    height: 6.8rem;
                    font-weight: 600;
                    width: 100%;
                    display: -webkit-box;
                    display: -ms-flexbox;
                    display: flex;
                    -webkit-box-orient: horizontal;
                    -webkit-box-direction: normal;
                    -ms-flex-direction: row;
                    flex-direction: row;
                    -webkit-box-pack: start;
                    -ms-flex-pack: start;
                    justify-content: flex-start;
                    -ms-flex-wrap: nowrap;
                    flex-wrap: nowrap;
                    -ms-flex-item-align: center;
                    align-self: center;
                    -webkit-box-align: center;
                    -ms-flex-align: center;
                    align-items: center;
                    overflow: hidden;
                    overflow-x: scroll;

                    @media (min-width: 75em){
                        width: 110rem;
                    }

                    @media (min-width: 87.5em){
                        width: 120rem;
                    }

                    @media (min-width: 114.0625em){
                        width: 135rem;
                    }

                    .inline-nav__item {
                        -ms-flex-negative: 0;
                        flex-shrink: 0;
                        padding: 0 2rem 0 0;
                        display: inline-block;
                        -webkit-transition: color 3s cubic-bezier(0,1,.75,1);
                        transition: color 3s cubic-bezier(0,1,.75,1);

                        @media (min-width: 28.125em){
                            padding-left: .5rem;
                        }
                        
                        @media (min-width: 28.125em){
                            padding-left: 0;
                        }

                        .inline-nav__link {
                            color: inherit;
                            text-decoration: none;
                            position: relative;
                            height: 100%;
                            -webkit-transition: color .2s cubic-bezier(0,1,.75,1);
                            transition: color .2s cubic-bezier(0,1,.75,1);
                            padding-bottom: .5rem;
                            color: hsla(0,0%,100%,.5);
                            border-bottom: .3rem solid transparent;
                            cursor: pointer;

                            &.inline-nav__link--active {
                                
                            }
                        }

                        &.inline-nav__item--active{
                            .inline-nav__link {
                                -webkit-transition: padding .2s cubic-bezier(0,1,.75,1);
                                transition: padding .2s cubic-bezier(0,1,.75,1);
                                border-bottom: .3rem solid #db0a40;
                                color: #fff;
                            }
                        }
                    }
                }
            }
        }
    }

    .content-container {

        position: relative;
        margin: 0 auto;
        min-height: .1rem;
        width: 110rem;
        max-width: 100%;
        padding: 0 4rem;

        @media (min-width: 75em){
            width: 110rem;
        }

        @media (min-width: 87.5em){
            width: 120rem;
        }

        @media (min-width: 114.0625em){
            width: 135rem;
        }

        .segment {
            position: relative;

            &:first-child {
                padding-top: 0;
            }

            &:first-child{
                .content-container__block {
                    .heading--section {
                    }
                }    
            }

            .content-container__block {
                .heading--section {
                    display: inline-block;
                }

                .collection__item--link {
                    span {
                        display: inline-block;
                        color: hsla(0,0%,100%,.8);
                        font-size: 1.2rem;
                        background: hsla(0,0%,100%,.1);
                        border-radius: 10px;
                        padding: .25rem 1rem 0.25rem 1rem;
                        text-transform: uppercase;
                        margin: 2rem 0 2.5rem 2rem;
                        vertical-align: middle;
                        line-height: 2rem;
                        font-weight: bold;

                        .icon {
                            font-weight: normal;
                            margin-right: 5px;
                            font-size: 10px;
                        }
                    }
                }
            }    
                
            .static-grid {
                position: relative;

                .static-grid__items {
                    display: -webkit-box;
                    display: -ms-flexbox;
                    display: flex;
                    -webkit-box-orient: horizontal;
                    -webkit-box-direction: normal;
                    -ms-flex-flow: row wrap;
                    flex-flow: row wrap;
                    margin: 0 -1rem;
                  
                    .static-grid__item {

                        @media (min-width: 60em){
                            padding-left: 2rem;
                            padding-right: 2rem;
                        }

                        .video-card {
                            height: 0;
                            padding-bottom: 150%;
                            -webkit-backface-visibility: hidden;
                            -webkit-transform: translateZ(0);
                            position: relative;
                            overflow: hidden;
                            margin-bottom: 2rem;
                            cursor: pointer;
                            padding-bottom: 0;
                            padding-top: calc(56.25% + 9rem);

                            @media (min-width: 60em){
                                padding-top: calc(66.66666667% + 9rem);
                            }

                            .video-card__link {
                                color: inherit;
                                text-decoration: none;
                                display: -webkit-box;
                                display: -webkit-flexbox;
                                display: -ms-flexbox;
                                display: flex;
                                position: absolute;
                                top: 0;
                                bottom: 0;
                                left: 0;
                                right: 0;
                                overflow: hidden;
                                -webkit-backface-visibility: hidden;
                                backface-visibility: hidden;

                                .video-card .image {
                                    background-size: contain;

                                    -webkit-transition-duration: .4s;
                                    transition-duration: .4s;
                                }

                                .image__image {
                                    background-position: center;
                                    background-size: cover;
                                }

                                .preview-video {
                                    height: 60%;
                                    overflow: hidden;
                                    z-index: 0;
                                    background: transparent;
                                    -webkit-transition: opacity 1s cubic-bezier(0,1,.75,1);
                                    transition: opacity 1s cubic-bezier(0,1,.75,1);
                                    opacity: 0;
                                }

                                .video-card__date-info {
                                    position: absolute;
                                    font-size: 1.5rem;
                                    top: 2rem;
                                    left: 2rem;
                                    font-weight: bold;
                                    color: #fff;

                                    .day {
                                        font-size: 3rem;
                                        font-weight: 800;
                                        letter-spacing: -.03em;
                                        text-shadow: 2px 2px 20px rgba(0,0,0,.25);
                                    }

                                    .time {
                                        font-size: 1.3rem;
                                        font-weight: 800;
                                        white-space: nowrap;
                                        overflow: hidden;
                                        text-overflow: ellipsis;
                                        text-transform: uppercase;
                                        text-shadow: 0 0 4px rgba(0,0,0,.25);
                                        color: #fff;
                                    }
                                }

                                .video-card__content {
                                    position: relative;
                                    z-index: 100;
                                    -webkit-align-self: flex-end;
                                    -ms-flex-item-align: end;
                                    align-self: flex-end;
                                    width: 100%;
                                    height: 9rem;
                                    padding: 2rem;
                                    background: #e2e3e5;
                                    line-height: 1.2;

                                    @media (min-width: 60em){
                                        -webkit-transition-property: height,background-color;
                                        transition-property: height,background-color;
                                        -webkit-transition-duration: .15s;
                                        transition-duration: .15s;
                                        -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                                        transition-timing-function: cubic-bezier(0,1,.75,1);
                                        background: ${ palette('primary', 2) };
                                    }

                                    .video-card__time {
                                        position: absolute;
                                        top: -2em;
                                        right: 2rem;
                                        font-size: 1.6rem;
                                        font-weight: 500;
                                        letter-spacing: .01em;
                                        color: #fff;
                                        text-shadow: 0 0 4px rgba(0,0,0,.5);
                                    }

                                    .video-card__heading {
                                        white-space: nowrap;
                                        overflow: hidden;
                                        text-overflow: ellipsis;
                                        color: #2c2f36;
                                        font-weight: 600;

                                        @media (min-width: 60em){
                                            -webkit-transition: color .15s cubic-bezier(0,1,.75,1);
                                            transition: color .15s cubic-bezier(0,1,.75,1);
                                            color: #e2e3e5;
                                        }
                                    }

                                    .video-card__description {
                                        white-space: nowrap;
                                        overflow: hidden;
                                        text-overflow: ellipsis;
                                        -webkit-backface-visibility: hidden;
                                        backface-visibility: hidden;
                                        font-size: 1.4rem;
                                        line-height: 1.1;
                                        font-weight: 500;
                                        color: #6f7279;
                                        margin-bottom: 1rem;

                                        @media (min-width: 75em){
                                            font-size: 1.6rem;
                                        }
                                    }
                                    
                                    .video-card__long-description {
                                        position: absolute;
                                        left: 0;
                                        padding: 0 2rem 2rem;
                                        overflow: hidden;
                                        font-size: 1.3rem;
                                        line-height: 1.4;
                                        max-height: 4.2em;
                                        display: -webkit-box;
                                        -webkit-line-clamp: 3;
                                        -webkit-box-orient: vertical;
                                        color: #0a1b2b;
                                        opacity: 0;

                                        @media (min-width: 60em){
                                            -webkit-transition: opacity .15s cubic-bezier(0,1,.75,1);
                                            transition: opacity .15s cubic-bezier(0,1,.75,1);
                                            font-size: 1.4rem;
                                        }

                                        @media (min-width: 87.5em){
                                            font-size: 1.6rem;
                                        }
                                    }
                                }

                                .video-card__play-button {
                                    position: absolute;
                                    top: -7rem;
                                    left: 0;
                                    right: 0;
                                    bottom: 0;
                                    z-index: 100;
                                    display: -webkit-box;
                                    display: -webkit-flexbox;
                                    display: -ms-flexbox;
                                    display: flex;
                                    -webkit-box-align: center;
                                    -ms-flex-align: center;
                                    align-items: center;
                                    -webkit-box-pack: center;
                                    -ms-flex-pack: center;
                                    justify-content: center;
                                    width: 6.66rem;
                                    height: 6.66rem;
                                    margin: auto;
                                    border-radius: 50%;
                                    background: rgba(0,0,0,.3);

                                    @media (min-width: 60em){
                                        -webkit-transition-property: opacity,border,-webkit-transform;
                                        transition-property: opacity,border,-webkit-transform;
                                        transition-property: opacity,border,transform;
                                        transition-property: opacity,border,transform,-webkit-transform;
                                        -webkit-transition-duration: .3s;
                                        transition-duration: .3s;
                                        -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                                        transition-timing-function: cubic-bezier(0,1,.75,1);
                                        width: 6rem;
                                        height: 6rem;
                                        -webkit-transform: scale3d(.7,.7,1) translate3d(0,1rem,0);
                                        transform: scale3d(.7,.7,1) translate3d(0,1rem,0);
                                        opacity: 0;
                                        border: 0;
                                        background: rgba(0,0,0,.3);
                                        top: -13.5rem;
                                    }

                                    @media (min-width: 75em){
                                        top: -14rem;
                                        width: 7rem;
                                        height: 7rem;
                                    }

                                    @media (min-width: 87.5em){
                                        top: -15.5rem;
                                    }

                                    .play-icon {
                                        font-size: 25px;
                                        margin-top: 4px;
                                        margin-left: 4px;
                                    }
                                }
                            }

                            &:hover{
                                .image {
                                    opacity: .65;
                                    -webkit-transform: scale3d(1.15,1.15,1);
                                    transform: scale3d(1.15,1.15,1);
                                    -webkit-transition-duration: .4s;
                                    transition-duration: .4s;
                                    -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                                    transition-timing-function: cubic-bezier(0,1,.75,1);
                                }

                                .video-card__content {
                                    @media (min-width: 60em){
                                        background: #e2e3e5;
                                    }

                                    .video-card__heading {
                                        color: #db0a40;
                                    }

                                    .video-card__long-description {
                                        opacity: 1;
                                    }
                                }

                                .video-card__play-button {
                                    opacity: 1;
                                    -webkit-transform: scaleX(1) translateZ(0);
                                    transform: scaleX(1) translateZ(0);
                                }
                            }
                        }

                        .video-card--has-description {

                            &:hover{
                                .video-card__content{
                                    @media (min-width: 60em){
                                        height: 14rem;
                                    }

                                    @media (min-width: 75em){
                                        height: 15rem;
                                    }

                                    @media (min-width: 87.5em) {
                                        height: 16rem;
                                    }
                                }
                            }
                        }

                        .channel-card-alt {
                            position: relative;
                            width: 100%;
                            margin-bottom: 2rem;
                            height: 0;
                            padding-bottom: 40%;
                            overflow: hidden;
                            line-height: 1;
                            cursor: pointer;
                            background: ${ palette('primary', 2) };
                    
                            @media (min-width: 60em) {
                                height: 0;
                                padding-bottom: 56.25%;
                            }
                    
                            @media (min-width: 75em){
                                height: 0;
                                padding-bottom: 66.667%;
                            }
                    
                            .channel-card-alt__link {
                                color: inherit;
                                text-decoration: none;
                                display: -webkit-box;
                                display: -ms-flexbox;
                                display: flex;
                                -webkit-box-align: center;
                                -ms-flex-align: center;
                                align-items: center;
                                -webkit-box-pack: center;
                                -ms-flex-pack: center;
                                justify-content: center;
                                position: absolute;
                                top: 0;
                                bottom: 0;
                                left: 0;
                                right: 0;
                    
                                .channel-card-alt__image {
                                    overflow: hidden;
                                    -webkit-transition-property: -webkit-transform;
                                    transition-property: -webkit-transform;
                                    transition-property: transform;
                                    transition-property: transform,-webkit-transform;
                                    -webkit-transition-duration: .6s;
                                    transition-duration: .6s;
                                    -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                                    transition-timing-function: cubic-bezier(0,1,.75,1);
                                    min-width: 101%;
                                    margin: auto;
                    
                                    &:before {
                                        content: "";
                                        position: absolute;
                                        top: 0;
                                        right: 0;
                                        bottom: 0;
                                        left: 0;
                                        background: -webkit-gradient(linear,left bottom,left top,color-stop(0,rgba(17,22,31,.5)),to(rgba(17,22,31,0)));
                                        background: linear-gradient(0deg,rgba(17,22,31,.5),rgba(17,22,31,0));
                                        z-index: 9;
                                    }
                    
                                    .channel-card-alt__overlay {
                                        position: absolute;
                                        z-index: 1;
                                        top: 0;
                                        left: 0;
                                        right: 0;
                                        bottom: 0;
                                        background: #000;
                                        opacity: .2;
                                        -webkit-transition: opacity .15s cubic-bezier(0,1,.75,1);
                                        transition: opacity .15s cubic-bezier(0,1,.75,1);
                                    }
                                }
                                
                                .channel-card-alt__heading {
                                    margin: 0;
                                    padding: 0;
                                    border: 0;
                                    vertical-align: baseline;
                                    font-family: inherit;
                                    font-size: inherit;
                                    font-weight: inherit;
                                    line-height: inherit;
                                    width: auto;
                                    color: #fff;
                                    position: relative;
                                    z-index: 10;
                                    margin: 0 1rem -.4rem;
                                    font-size: 2.3rem;
                                    font-weight: 800;
                                    text-transform: uppercase;
                                    text-align: center;
                    
                                    &:after {
                                        display: block;
                                        content: "";
                                        position: relative;
                                        top: .5rem;
                                        border-bottom: .3rem solid #fff;
                                        opacity: 0;
                                        -webkit-transform: translateY(1rem);
                                        transform: translateY(1rem);
                                        -webkit-transition: all .15s;
                                        transition: all .15s;
                                    }
                    
                                    @media (min-width: 60em){
                                        font-size: 2rem;
                                    }
                    
                                    @media (min-width: 75em){
                                        font-size: 2.5rem;
                                    }
                    
                                    @media (min-width: 114.0625em) {
                                        font-size: 3rem;
                                    }
                                }
                    
                            }   
                    
                            &:hover{
                    
                                .channel-card-alt__link{
                                    .channel-card-alt__image {
                                        -webkit-transform: scale3d(1.1,1.1,1);
                                        transform: scale3d(1.1,1.1,1);
                    
                                        .channel-card-alt__overlay {
                                            opacity: .6;
                                        }
                                    }
                    
                                    .channel-card-alt__heading:after {
                                        opacity: 1;
                                        -webkit-transform: translateY(0);
                                        transform: translateY(0);
                                    }
                                }
                    
                            }
                        }

                        .standard-card {
                            height: 0;
                            padding-bottom: 150%;
                            -webkit-backface-visibility: hidden;
                            -webkit-transform: translateZ(0);
                            position: relative;
                            overflow: hidden;
                            cursor: pointer;
                            margin-bottom: 2rem;
                            background: #000;

                            .standard-card__link {
                                color: inherit;
                                text-decoration: none;
                                display: -webkit-box;
                                display: -ms-flexbox;
                                display: flex;
                                -webkit-box-orient: vertical;
                                -webkit-box-direction: normal;
                                -ms-flex-direction: column;
                                flex-direction: column;
                                position: absolute;
                                top: 0;
                                bottom: 0;
                                left: 0;
                                right: 0;
                                overflow: hidden;
                                height: 100%;
                                width: 100%;
                                -webkit-backface-visibility: hidden;
                                backface-visibility: hidden;

                                .standard-card__image {
                                    -webkit-transition-property: opacity,-webkit-transform;
                                    transition-property: opacity,-webkit-transform;
                                    transition-property: opacity,transform;
                                    transition-property: opacity,transform,-webkit-transform;
                                    -webkit-transition-duration: .6s;
                                    transition-duration: .6s;
                                    -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                                    transition-timing-function: cubic-bezier(0,1,.75,1);

                                    .image__image {
                                        -webkit-transform: scale(1.01);
                                        transform: scale(1.01);
                                        ackground-position: top;
                                    }
                                }
                            }

                            &:hover .image {
                                -webkit-transform: scale3d(1.15,1.15,1);
                                transform: scale3d(1.15,1.15,1);
                            }
                        }

                    }

                    .static-grid__item {
                        padding-left: 1rem;
                        padding-right: 1rem;
                    }
                }
            }

            .live-events-item {
                text-align: left;

                .live-events-item__link {
                    display: block;
                    color: inherit;
                    text-decoration: none;
                    position: relative;
                    margin: 3rem 0;
                    background: ${ palette('primary', 2) };
                    -webkit-transition: background .15s;
                    transition: background .15s;

                    @media (min-width: 28.125em){
                        min-height: 32rem;
                    }

                    &:after, :before {
                        display: table;
                        content: "";
                    }

                    .live-events-item__image {
                        height: 0;
                        padding-bottom: 56.25%;
                        position: relative;
                        overflow: hidden;

                        @media (min-width: 60em) {
                            position: absolute;
                            right: 0;
                            top: 0;
                            bottom: 0;
                            left: 40rem;
                            margin: auto;
                            height: 100%;
                            padding-bottom: 0;
                        }

                        @media (min-width: 75em) {
                            left: 40rem;
                        }

                        .live-events-item__background {
                            position: absolute;
                            top: 0;
                            bottom: 0;
                            left: 0;
                            right: 0;
                            background-position: 50% 50%;
                            background-size: cover;
                            -webkit-transform: scale(1);
                            transform: scale(1);
                            -webkit-transition: opacity .3s,-webkit-transform 1.5s;
                            transition: opacity .3s,-webkit-transform 1.5s;
                            transition: opacity .3s,transform 1.5s;
                            transition: opacity .3s,transform 1.5s,-webkit-transform 1.5s;
                            -webkit-transition-timing-function: cubic-bezier(0,1,.75,1);
                            transition-timing-function: cubic-bezier(0,1,.75,1);
                            opacity: 1;
                        }
                    }

                    .live-events-item__content {
                        position: relative;
                        display: -webkit-box;
                        display: -ms-flexbox;
                        display: flex;
                        -webkit-box-orient: vertical;
                        -webkit-box-direction: normal;
                        -ms-flex-direction: column;
                        flex-direction: column;
                        height: 100%;
                        padding: 1.5rem 2rem 0;

                        @media (min-width: 28.125em) {
                            padding: 2rem 2rem 0;
                        }

                        @media (min-width: 75em) {
                            padding: 2rem 4rem 0;
                            width: 40rem;
                            min-height: 32rem;
                        }

                        .live-events-item__date {
                            line-height: 1;

                            @media (min-width: 60em) {
                                margin-bottom: .75rem;
                            }
                        }

                        .live-events-item__time {
                            -webkit-box-flex: 1;
                            -ms-flex-positive: 1;
                            flex-grow: 1;

                            font-size: 1.4rem;
                            font-weight: 700;
                            margin-bottom: 3rem;
                            color: #6f7279;

                            @media (min-width: 60em) {
                                font-size: 1.6rem;
                                margin-bottom: 0;
                            }
                        }

                        .live-events-item__detail {
                            -webkit-box-flex: 0;
                            -ms-flex-positive: 0;
                            flex-grow: 0;

                            .live-events-item__heading-group {
                                line-height: 1.35;
                                padding-bottom: 2rem;

                                @media (min-width: 60em) {
                                    padding-bottom: 4rem;
                                }

                                .live-events-item__heading {
                                    margin: 0;
                                    padding: 0;
                                    border: 0;
                                    vertical-align: baseline;
                                    font-family: inherit;
                                    font-size: inherit;
                                    font-weight: inherit;
                                    line-height: inherit;
                                    width: auto;
                                    font-size: 1.7rem;
                                    line-height: 1.2;
                                    font-weight: 700;
                                    color: inherit;
                                }

                                .live-events-item__subheading {
                                    font-size: 1.3rem;
                                    font-weight: 500;
                                    color: #6f7279;

                                    @media (min-width: 20.625em) {
                                        font-size: 1.4rem;
                                    }

                                    @media (min-width: 60em) {
                                        font-size: 1.5rem;
                                    }
                                }
                            }
                        }

                        .live-events-item__cta {
                            position: relative;
                            display: inline-block;
                            color: #e6e7e9;
                            padding: .75rem 0 2rem;

                            @media (min-width: 48em) {
                                padding-top: 1.5rem;
                                padding-bottom: 3rem;
                            }

                            &:before {
                                content: "";
                                position: absolute;
                                top: 0;
                                left: 0;
                                display: block;
                                width: 1.8rem;
                                height: .2rem;
                                background: #666f7a;
                            }
                        }

                    }

                    &:hover{
                        .live-events-item__image{
                            .live-events-item__background {
                                @media (min-width: 60em) {
                                    opacity: .85;
                                    -webkit-transform: scale(1.05);
                                    transform: scale(1.05);
                                }
                            }
                        }

                        .live-events-item__content {
                            .live-events-item__detail{
                                .live-events-item__heading-group {
                                    .live-events-item__heading {
                                        color: #db0a40;
                                    }
                                }
                            }   
                        }
                    }
                }

                &:first-of-type .live-events-item__link {
                    @media (min-width: 60em) {
                            margin-top: 0;
                    }
                }
            }

        }
    }
`;

export default VideoCategoriesHolder;
