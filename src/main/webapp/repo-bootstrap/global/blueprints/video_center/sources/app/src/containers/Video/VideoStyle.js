import styled from 'styled-components';
import { palette } from 'styled-theme';

const VideoHolder = styled.div`
    .video-details {
        position: relative;
        margin: 0 auto;
        min-height: .1rem;
        width: 110rem;
        max-width: 100%;
        padding: 0 4rem;
        color: inherit;
        padding-top: 3rem;

        @media (min-width: 75em){
            width: 110rem;
        }

        @media (min-width: 87.5em){
            width: 120rem;
        }

        @media (min-width: 114.0625em){
            width: 135rem;
        }

        .video-details__header {
            width: 100%;
            display: -webkit-box;
            display: -ms-flexbox;
            display: flex;
            -ms-flex-wrap: wrap;
            flex-wrap: wrap;
            -webkit-box-align: top;
            -ms-flex-align: top;
            align-items: top;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            justify-content: space-between;
            margin: 0 0 .5rem;
            padding: 0;

            position: relative;
            // margin: 0 auto;
            min-height: .1rem;
            // width: 110rem;
            max-width: 100%;
            padding: 0 4rem 0 0;
            color: inherit;
            padding-top: 3rem;

            &:before, :after{
                display: table;
                content: "";
            }

            .video-details__titles {
                -webkit-box-flex: 1;
                -ms-flex-positive: 1;
                flex-grow: 1;

                .video-details__thumbnail {
                    position: absolute;
                    width: 5rem;
                    height: 5rem;
                    margin: 0 1rem 0 0;

                    .image, .image__image {
                        border-radius: 50%;
                    }
                }

                .video-details__titles-content{
                    // padding-left: 6rem;

                    h1.video-details__heading {
                        font-weight: 800;
                        font-family: inherit;
                        margin: 0 0 .3rem;
                    }
                }

                .video-details__episode {
                    margin: 0!important;
                    padding: 0!important;
                    border: 0!important;
                    vertical-align: baseline!important;
                    font-family: inherit!important;
                    font-size: inherit!important;
                    font-weight: inherit!important;
                    line-height: inherit!important;
                    width: auto!important;
                    margin-bottom: 1.5rem!important;
                    font-size: 1.5rem!important;
                    font-weight: 500!important;
                    color: #6f7279!important;

                    @media (min-width: 48em){
                        font-size: 1.6rem!important;
                    }
                }
            }

            .video-details__links {
                margin: 0 0 1rem;
                display: -webkit-box;
                display: -ms-flexbox;
                display: flex;
                -ms-flex-wrap: wrap;
                flex-wrap: wrap;

                .inline-button {
                    text-decoration: none;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    height: 6rem;
                    line-height: 6rem;
                    width: 15rem;
                    max-width: 100%;
                    font-size: 1.3rem;
                    font-weight: 700;
                    text-align: center;
                    text-transform: uppercase;
                    cursor: pointer;
                    -webkit-user-select: none;
                    -moz-user-select: none;
                    -ms-user-select: none;
                    user-select: none;
                    position: relative;
                    display: inline-block;
                    white-space: nowrap;
                    width: auto!important;
                    height: auto;
                    line-height: inherit;
                    overflow: visible;
                    color: inherit;
                    -webkit-transition: color .15s cubic-bezier(0,1,.75,1);
                    transition: color .15s cubic-bezier(0,1,.75,1);

                    @media (min-width: 28.125em){
                        font-size: 1.4rem;
                        width: 20rem;
                    }

                    &:before, after{
                        content: "";
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        margin: auto;
                    }

                    .inline-button__text {
                        color: #fff;
                        position: relative;
                        left: 0;
                        -webkit-transform: translateZ(0);
                        transform: translateZ(0);
                        -webkit-transition: left .15s;
                        transition: left .15s;
                    }
                    .inline-button__text {
                        z-index: 993;
                    }

                    .inline-button__icon {
                        margin-right: .5rem;
                    }
                }

                .inline-button__text {
                    z-index: 993;
                }

                .inline-button__text {
                    color: #fff;
                    position: relative;
                    left: 0;
                    -webkit-transform: translateZ(0);
                    transform: translateZ(0);
                    -webkit-transition: left .15s;
                    transition: left .15s;
                }

                .video-details__links-link {
                    padding: 1rem 2rem 0 0;

                    @media (min-width: 48em){
                        padding-top: .5rem;
                    }
                }

                a {
                    line-height: 0;
                }
            }
            
            .video-details__description{
                font-size: 1.8rem;
                width: 100%;

                &:after{
                    display: table;
                    content: "";
                }

                p {
                    max-width: 75%;
                    float: left;
                    margin: 0;
                    padding: 0;
                    border: 0;
                    vertical-align: baseline;
                    font-family: inherit;
                    font-size: inherit;
                    font-weight: inherit;
                    line-height: inherit;
                    width: auto;
                }
            }
        }

        hr {
            height: 0;
            margin: 0;
            border: 0;
            border-bottom: 1px solid ${ palette('primary', 1) };
        }
    }

    .app-content__sidebar{
        opacity: 0;
        -webkit-box-flex: 0;
        -ms-flex: 0 0 0px;
        flex: 0 0 0;
        -webkit-box-ordinal-group: 3;
        -ms-flex-order: 2;
        order: 2;
        -ms-flex-negative: 0;
        flex-shrink: 0;
        min-width: 0;
        height: 100vh;
        background: ${ palette('primary', 1) };
        -webkit-transform: translateX(100%);
        transform: translateX(100%);
    
        &.app-content__sidebar--animate{
            -webkit-transition: opacity .15s ease-out,-webkit-box-flex .15s ease-out,-webkit-transform .15s ease-out;
            transition: opacity .15s ease-out,-webkit-box-flex .15s ease-out,-webkit-transform .15s ease-out;
            transition: flex .15s ease-out,transform .15s ease-out,opacity .15s ease-out;
            transition: flex .15s ease-out,transform .15s ease-out,opacity .15s ease-out,-webkit-box-flex .15s ease-out,-ms-flex .15s ease-out,-webkit-transform .15s ease-out;
        }
    
        &.app-content__sidebar--visible {
            opacity: 1;
            -webkit-box-flex: 0;
            -ms-flex: 0 0 43rem;
            flex: 0 0 43rem;
            -webkit-transform: translateX(0);
            transform: translateX(0);
        }
    }
`;

export default VideoHolder;
