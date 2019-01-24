import styled from 'styled-components';

const VideoPlayerHolder = styled.div`
    #app-content__player{
        max-height: calc(100vh - 9rem);
    }

    .app-content__player {
        background: #000;
        max-height: 75vh;
        z-index: 1000;

        @media (min-width: 114.0625em){
            max-height: 75vh;
        }

        @media (min-width: 114.0625em){
            max-height: 75vh;
        }

        @media (max-height: 47.9375em){
            max-height: 80vh;
        }

        .app-content__player-wrapper{
            width: 100%;
            padding: 0px;

            @media (min-width: 114.0625em){
                position: relative;
                margin: 0 auto;
                min-height: .1rem;
                width: 110rem;
                max-width: 100%;
                padding: 0 4rem;
            }

            @media (min-width: 114.0625em) and (min-width: 75em){
                width: 110rem;
            }

            @media (min-width: 114.0625em) and (min-width: 87.5em){
                width: 120rem;
            }

            @media (min-width: 114.0625em){
                width: 135rem;
            }

            .global-video-player {
                -webkit-backface-visibility: hidden;
                backface-visibility: hidden;
                display: none;
                margin: 0 auto;
                -o-object-fit: contain;
                object-fit: contain;
                -webkit-transition: all .1s ease-in;
                transition: all .1s ease-in;
                width: 100%;
                height: 100%;
                height: 0;
                padding-bottom: 56.25%;

                min-width: 160px;
                min-height: 90px;
                max-width: 980px;
                max-height: 551px;

                &.global-video-player--visible {
                    opacity: 1;
                    display: block;
                }

                &.global-video-player--fixed {
                    z-index: 1000;
                    position: fixed;
                    padding-bottom: 0;
                    right: 1.5rem;
                    bottom: 1.5rem;
                    width: 18rem;
                    height: auto;
                    -webkit-box-shadow: 0 3px 6px rgba(0,0,0,.16), 0 3px 6px rgba(0,0,0,.23);
                    box-shadow: 0 3px 6px rgba(0,0,0,.16), 0 3px 6px rgba(0,0,0,.23);
                    -webkit-animation: showVideoFixed 1s;
                    animation: showVideoFixed 1s;

                    @media (min-width: 28.125em){
                        width: 25rem;
                    }

                    @media (min-width: 48em){
                        right: 2rem;
                        bottom: 2rem;
                        width: 30rem;
                    }
                }

                .global-video-player__aspect {
                    height: 0;
                    padding-bottom: 56.25%;
                    position: relative;

                    .global-video-player__inner {
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        width: 100%;
                        height: 100%;

                        .player-container{
                            opacity: 1;
                            position: relative;
                            width: 100% !important;
                            height: 100% !important;
                            overflow: hidden;
                            -webkit-touch-callout: none;
                            -webkit-user-select: none;
                            -moz-user-select: none;
                            -ms-user-select: none;
                            user-select: none;
                            background-color: #000;
                        }
                    }
                }
            }
        }
    }

    .global-video-player__close {
        display: block;
        position: absolute;
        z-index: 1000;
        top: 1rem;
        right: 1rem;
        width: 2.6rem;
        height: 2.6rem;
        line-height: 2.7rem;
        color: rgba(0,0,0,.5);
        text-align: center;
        cursor: pointer;
        border-radius: 50%;
        background: hsla(0,0%,100%,.5);
        opacity: 0;
        -webkit-transform: translateY(10%);
        transform: translateY(10%);
    }

    .global-video-player--fixed .global-video-player__close {
        opacity: 1;
        -webkit-transform: translateY(0);
        transform: translateY(0);
    }

    .global-video-player--fixed .fixed-player__link-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 999;
    }

    .player-container{
        #giantPlayButtonContainer {
            position: absolute;
            margin: auto;
            width: 200px;
            height: 200px;
            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
        }

        #bufferingSpinner {
            position: absolute;
            margin: auto;
            width: 100px;
            height: 100px;
            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
        }

        .for-screen-readers {
            position: absolute;
            left: -10000px;
            top: auto;
            width: 1px;
            height: 1px;
            overflow: hidden;
        }

        #bufferingSpinner {
            margin: auto;
            width: 100px;
            height: 100px;

            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
        }

        #controlsContainer {
            position: absolute;
            width: 100%;
            height: 35px;
            padding: 0 5px;
            margin: 0 auto 5px;
            box-sizing: border-box;

            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;

            bottom: 5px;
            left: 0px;
        }

        #controls {
            width: 100%;
            max-width: 800px;
            height: 35px;
            margin: 0;
            padding: 0 0 0 7px;

            background-color: rgba(20, 20, 20, 0.8);
            border-radius: 5px;

            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: center;

            opacity: 0;
            transition: opacity 0.3s;
        }

        /* Show the controls when the mouse is over them.  This overrides the mouse
        * timeout logic in JS that is used to hide the controls when the mouse stops
        * moving over the video container.
        */
        &.player-container:hover #controls, #controls:hover {
            opacity: 1;
            z-index: 9999999999;    // Screenfull lib sets z-index: 2147483647; when in fullscreen, need to override in controls
        }

        /* NOTE: These fullscreen pseudo-classes can't be combined.  Browsers ignore
        * the rest of the list once they hit one prefix they don't support.
        */
        #videoContainer:fullscreen { width: 100%; height: 100%; }
        #videoContainer:-webkit-full-screen { width: 100%; height: 100%; }
        #videoContainer:-moz-full-screen { width: 100%; height: 100%; }
        #videoContainer:-ms-fullscreen { width: 100%; height: 100%; }

        #liveLabel {
            color: #ff0000;
            font-size: 1.5rem;
            font-weight: bold;
            margin-right: 10px;
        }

        #controls button {
            color: white;
            height: 32px;
            width: 32px;
            padding: 0;
            margin: 0 7px 0 0;
            background: transparent;
            border: 0;
            outline: none;
            border-radius: 4px;
            cursor: pointer;
        }

        #controls button:active {
            background: rgba(100, 100, 100, 0.4);
        }

        #controls button:disabled {
            color: rgba(255, 255, 255, 0.3);
        }

        #controls input[type="range"] {
            cursor: pointer;
        }

        #castReceiverName {
            display: none;

            background-color: rgba(0, 0, 0, 0.5);
            color: white;
            font-size: 150%;
            padding: 5px;

            bottom: 50px;
            left: 0;
            right: 0;
            margin: auto;
            width: max-content;
        }

        #giantPlayButtonContainer {
            margin: auto;
            width: 200px;
            height: 200px;

            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
        }

        #giantPlayButton {
            width: 100%;
            height: 100%;
            font-size: 150px;
            margin: 0;
            padding: 0;
            border: 0;
            outline: none;
            background: rgba(100, 100, 100, 0.4);
            border-radius: 40px;
        }

        #pauseButton, #unmuteButton,
        #castButton, #castConnectedButton,
        #rewindButton, #fastForwardButton,
        #giantPlayButtonContainer, #bufferingSpinner {
            display: none;
        }

        #currentTime {
            display: flex;
            flex-grow: 0;
            margin: 0 9px 0 0;
            font-family: sans-serif;
            font-size: 13px;
            font-weight: bold;
            color: white;

            cursor: default;
            user-select: none;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
        }


        /* Always show controls while casting */
        #controls.casting {
            opacity: 1;
        }

        /* Hide fullscreen button while casting */
        #controls.casting #fullscreenButton {
            display: none;
        }


        /* NOTE: pseudo-elements for different browsers can't be combined with commas.
        * Browsers will ignore styles if any pseudo-element in the list is unknown.
        */

        /* range inputs, common style */
        #seekBar, #volumeBar {
            display: flex;
            height: 7px;
            margin: 0 12px 0 0;
            padding: 0;

            /* removes webkit default styling */
            -webkit-appearance: none;

            border: 1px solid #666;
            border-radius: 4px;
            background-color: black;
            outline: none;
        }
        /* removes mozilla default styling */
        #seekBar::-moz-range-track, #volumeBar::-moz-range-track {
            background-color: transparent;
            outline: none;
        }
        /* removes IE default styling */
        #seekBar::-ms-track, #seekBar::-ms-fill-lower, #seekBar::-ms-fill-upper,
        #volumeBar::-ms-track, #volumeBar::-ms-fill-lower, #volumeBar::-ms-fill-upper {
            background-color: transparent;
            outline: none;
        }

        /* per-instance styles */
        #seekBar {
            flex-grow: 1;
        }
        #volumeBar {
            flex-grow: 0;
            min-width: 15px;
            max-width: 70px;
        }


        /* thumb pseudo-element, common style */
        #seekBar::-webkit-slider-thumb, #volumeBar::-webkit-slider-thumb {
            -webkit-appearance: none;
            background-color: white;
            outline: none;
        }
        #seekBar::-moz-range-thumb, #volumeBar::-moz-range-thumb {
            background-color: white;
            outline: none;
        }
        #seekBar::-ms-thumb, #volumeBar::-ms-thumb {
            background-color: white;
            outline: none;
        }

        /* thumb pseudo-element, seek style */
        #seekBar::-webkit-slider-thumb {
            width: 18px;
            height: 11px;
            border-radius: 8px;
        }
        #seekBar::-moz-range-thumb {
            width: 18px;
            height: 11px;
            border-radius: 8px;
        }
        #seekBar::-ms-thumb {
            width: 18px;
            height: 11px;
            border-radius: 8px;
        }

        /* thumb pseudo-element, volume style */
        #volumeBar::-webkit-slider-thumb {
            width: 12px;
            height: 12px;
            border-radius: 12px;
        }
        #volumeBar::-moz-range-thumb {
            width: 12px;
            height: 12px;
            border-radius: 12px;
        }
        #volumeBar::-ms-thumb {
            width: 12px;
            height: 12px;
            border-radius: 12px;
        }

        /* turn off tooltips for the seekBar on IE */
        #seekBar::-ms-tooltip {
            display: none;
        }

        /* hide volume and mute buttons on mobile-sized screens */
        @media screen and (max-width: 700px) {
            #volumeBar, #muteButton {
            display: none;
            }
        }


        /*
        The SVG/CSS buffering spinner is based on http://codepen.io/jczimm/pen/vEBpoL
        Some local modifications have been made.

        Copyright (c) 2016 by jczimm

        Permission is hereby granted, free of charge, to any person obtaining a copy of
        this software and associated documentation files (the "Software"), to deal in
        the Software without restriction, including without limitation the rights to
        use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
        of the Software, and to permit persons to whom the Software is furnished to do
        so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
        */

        .spinnerSvg {
            animation: rotate 2s linear infinite;
            height: 100%;
            transform-origin: center center;
            width: 100%;
            position: absolute;
            top: 0; bottom: 0; left: 0; right: 0;
            margin: auto;
        }

        .spinnerPath {
            /* Fall back for IE 11, where the stroke properties are not animated,
            but the spinner still rotates. */
            stroke: #d62d20;
            stroke-dasharray: 20, 200;
            stroke-dashoffset: 0;

            animation:
            dash 1.5s ease-in-out infinite,
            color 6s ease-in-out infinite;
            stroke-linecap: round;
        }

        @keyframes rotate {
            100% {
            transform: rotate(360deg);
            }
        }

        @keyframes dash {
            0% {
            stroke-dasharray: 1, 200;
            stroke-dashoffset: 0;
            }
            50% {
            stroke-dasharray: 89, 200;
            stroke-dashoffset: -35px;
            }
            100% {
            stroke-dasharray: 89, 200;
            stroke-dashoffset: -124px;
            }
        }

        @keyframes color {
            100%, 0% {
            stroke: #d62d20;
            }
            40% {
            stroke: #0057e7;
            }
            66% {
            stroke: #008744;
            }
            80%, 90% {
            stroke: #ffa700;
            }
        }
    }
`;

export default VideoPlayerHolder;
