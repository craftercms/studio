import styled from 'styled-components';
// import { palette, font } from 'styled-theme';

const SidebarHolder = styled.div`
    .video-sidebar{
        @media (min-width: 67.5em){
            display: block;
        }

        @media (min-width: 75em){
            width: 43rem;
        }

        .video-sidebar__tabs {
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
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            border-bottom: 1px solid #1e2632;

            &, .video-sidebar__tab {
                display: -webkit-box;
                display: -ms-flexbox;
                display: flex;
                height: 9rem;
            }

            .video-sidebar__tab {
                position: relative;
                -webkit-box-orient: horizontal;
                -webkit-box-direction: normal;
                -ms-flex-flow: row;
                flex-flow: row;
                -webkit-box-pack: center;
                -ms-flex-pack: center;
                justify-content: center;
                -webkit-box-align: center;
                -ms-flex-align: center;
                align-items: center;
                margin: 0 0 0 3rem;
                color: #686868;
                cursor: pointer;
                -webkit-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
                -webkit-transition: all .15s;
                transition: all .15s;

                &:before {
                    display: block;
                    content: "";
                    position: absolute;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    height: 0;
                    background: #db0a40;
                    -webkit-transition: all .15s;
                    transition: all .15s;
                }

                &.video-sidebar__tab--active {
                    &:before {
                        height: .5rem;
                    }
                }

                &, .video-sidebar__tab--active, .video-sidebar__tab:hover {
                    color: #fff;
                }
            }
        }

        .video-sidebar__content{
            display: none;
            position: absolute;
            top: 9.1rem;
            left: 0;
            right: 0;
            bottom: 0;
            opacity: 0;
            pointer-events: none;
            overflow: hidden;
            -webkit-transition: all .15s;
            transition: all .15s;

            &.video-sidebar__content--active {
                display: block;
                opacity: 1;
                pointer-events: all;
            }

            .video-sidebar__content-scroll {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                padding-top: 2rem;
                overflow-y: scroll;
                overflow-x: hidden;
                -webkit-transition: all .15s;
                transition: all .15s;

                .video-sidebar__content-inner--custom {
                    padding: 1rem 1rem 1rem 3rem;
                    height: 100%;
                    position: relative;

                    .playlist__item {
                        color: inherit;
                        text-decoration: none;
                        display: -webkit-box;
                        display: -ms-flexbox;
                        display: flex;
                        -webkit-box-orient: vertical;
                        -webkit-box-direction: normal;
                        -ms-flex-flow: column;
                        flex-flow: column;
                        -webkit-box-pack: center;
                        -ms-flex-pack: center;
                        justify-content: center;
                        position: relative;
                        margin: 0 0 2rem;
                        padding: 0 0 0 11.5rem;
                        min-height: 7rem;
                        -webkit-transition: all .15s;
                        transition: all .15s;

                        .playlist__image {
                            position: absolute;
                            top: 0;
                            left: 0;
                            height: 7rem;
                            width: 10rem;
                            background: #333;
                            overflow: hidden;

                            .image__image {
                                -webkit-transition: -webkit-transform .15s cubic-bezier(0,1,.75,1);
                                transition: -webkit-transform .15s cubic-bezier(0,1,.75,1);
                                transition: transform .15s cubic-bezier(0,1,.75,1);
                                transition: transform .15s cubic-bezier(0,1,.75,1),-webkit-transform .15s cubic-bezier(0,1,.75,1);
                            }
                        }

                        .playlist__status {
                            display: block;
                            font-size: 1.3rem;
                            line-height: 1.2;
                            letter-spacing: .03em;
                            font-weight: 500;
                            text-transform: uppercase;

                            .playlist__duration {
                                color: #6f7279;
                                font-size: 1.1rem;
                            }
                        }

                        .playlist__title {
                            font-size: 1.7rem;
                            line-height: 1.2;
                            font-weight: 700;
                            -webkit-transition: color .15s cubic-bezier(0,1,.75,1);
                            transition: color .15s cubic-bezier(0,1,.75,1);
                        }

                        .playlist__subtitle {
                            font-size: 1.3rem;
                            font-weight: 500;
                            letter-spacing: .01em;
                            color: #6f7279;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }

                        &[href]:hover {
                            .playlist__image {
                                .image__image {
                                    -webkit-transform: scale(1.2);
                                    transform: scale(1.2);
                                }
                            }
                            
                            .playlist__title {
                                color: #db0a40;
                            }
                        }
                    }
                }
            }
        }
    }

`;

export default SidebarHolder;
