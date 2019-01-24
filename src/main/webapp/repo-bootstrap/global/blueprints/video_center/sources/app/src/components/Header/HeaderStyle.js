import styled from 'styled-components';
import { palette } from 'styled-theme';

const HeaderHolder = styled.div`
    width:'100%';

    .header {
        height: 5.6rem;
        position: relative;
        z-index: 996;
        background: ${ palette('primary', 0) };
        color: hsla(0,0%,100%,.5);
        text-align: right;
        overflow: hidden;

        &:before, :after{
            display: table;
            content: "";
        }

        @media (min-width: 48em){
            height: 9rem;
            border-bottom: 1px solid ${ palette('primary', 2) };
        }

        .header__logo {
            color: inherit;
            text-decoration: none;
            position: absolute;
            top: 0;
            bottom: 0;
            left: 2rem;
            margin: auto;
            width: 7rem;
            height: 3rem;
            text-indent: 12.5rem;
            white-space: nowrap;
            overflow: hidden;
            background: transparent url(/img/redbull_tv_logo.svg) 0 0 no-repeat;
            background-size: contain;
            cursor: pointer;

            @media (min-width: 48em){
                position: relative;
                top: auto;
                bottom: auto;
                left: auto;
                float: left;
                width: 12.5rem;
                height: 5.1rem;
                margin-top: 1.95rem;
            }
        }

        &.header--ghost .header__logo {
            background-image: url(/img/brand_white.svg);
            background-color: transparent;
        }

        .header__container {
            position: relative;
            margin: 0 auto;
            min-height: .1rem;
            width: 110rem;
            max-width: 100%;
            padding: 0 4rem;
            position: static;

            &:after, :before {
                display: table;
                content: "";
            }

            @media (min-width: 48em){
                height: 9rem;
            }

            @media (min-width: 75em){
                width: 110rem;
            }

            @media (min-width: 87.5em){
                width: 120rem;
            }

            @media (min-width: 114.0625em){
                width: 135rem;
            }

            .header__overlay {
                display: none;
                position: fixed;
                z-index: 9;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: #000;
                opacity: 0;
            }

            .header__navigation {
                display: inline-block;
                vertical-align: top;
                height: 5.6rem;
                width: auto;
                margin: 0;
                visibility: visible;
                opacity: 1;

                @media (min-width: 60em){
                    display: inline-block;
                    float: left;
                    height: 9rem;
                    width: auto;
                    visibility: visible;
                    opacity: 1;
                    pointer-events: all;
                    -webkit-transform: translateY(0);
                    transform: translateY(0);
                }

                .navigation {
                    width: 100%;
                    line-height: 5.6rem;
                    height: 5.6rem;
                    text-align: right;
                    overflow: hidden;

                    @media (min-width: 60em){
                        text-align: left; 
                    }

                    @media (min-width: 48em){
                        line-height: 9rem;
                        height: 9rem;
                    }

                    .navigation__list {
                        margin: 0;
                        padding: 0;
                        border: 0;
                        vertical-align: baseline;
                        font-family: inherit;
                        font-size: inherit;
                        font-weight: inherit;
                        width: auto;
                        list-style: none;
                        line-height: inherit;
                        height: inherit;

                        @media (min-width: 67.5em){
                            padding-left: 4rem;
                        }

                        .navigation__item {
                            display: inline-block;
                            vertical-align: top;

                            .navigation__link {
                                color: inherit;
                                text-decoration: none;
                                display: block;
                                position: relative;
                                color: hsla(0,0%,100%,.7);
                                font-weight: 700;
                                font-size: 1.5rem;
                                text-transform: uppercase;
                                -webkit-transition: color .15s linear;
                                transition: color .15s linear;
                                width: 2rem;
                                height: inherit;
                                line-height: inherit;
                                margin-right: 3rem;

                                @media (min-width: 60em){
                                    width: auto;
                                    height: auto;
                                    padding-left: 0;
                                    padding-right: 0;
                                    font-size: 1.6rem;
                                    text-align: left;
                                }

                                &:hover {
                                    color: #fff;
                                    fill: hsla(0,0%,100%,.5);
                                }
                            }
                        }
                    }
                }
            }

            .header__search {
                display: inline-block;
                height: 5.6rem;
                line-height: 5.6rem;
                overflow: hidden;
                cursor: pointer;
                -webkit-transition: color .15s cubic-bezier(0,1,.75,1);
                transition: color .15s cubic-bezier(0,1,.75,1);
                clear: none;

                @media (min-width: 48em){
                    height: 9rem;
                    line-height: 9rem;
                }

                &:hover {
                    color: #fff;
                    fill: hsla(0,0%,100%,.5);
                }

                .search__icon {
                    width: 2rem;
                    height: 2rem;
                    vertical-align: middle;
                    margin-top: -.5rem;
                }
            }
        }

        &.header--ghost {
            margin-bottom: -5.6rem;
            border-bottom: none;
            background: transparent;

            @media (min-width: 48em){
                margin-bottom: -9rem;
            }

            @media (min-width: 48em){
                .header__navigation  {
                  color: inherit;
                }
            }
        }
    }

`;

export default HeaderHolder;
