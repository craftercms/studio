import styled from 'styled-components';
import { palette } from 'styled-theme';

const ChannelsHolder = styled.div`
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
`;

export default ChannelsHolder;
