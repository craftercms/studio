import styled from 'styled-components';
import { palette } from 'styled-theme';

const AppHolder = styled.div`
    background: ${ palette('primary', 0) };

    min-height: 100vh;
    position: relative;

    @media (min-width: 75em){
        display: -webkit-box;
        display: -ms-flexbox;
        display: flex;
    }

    .app-content__cont {
        height: 100vh;
        width: 100vw;
        overflow-x: hidden;
        position: relative;

        @media (min-width: 75em){
            display: -webkit-box;
            display: -ms-flexbox;
            display: flex;
        }

        .app-content__main {
            position: relative;
            -webkit-box-ordinal-group: 2;
            -ms-flex-order: 1;
            order: 1;
            -webkit-box-flex: 3;
            -ms-flex: 3 0 0px;
            flex: 3 0 0;
            -webkit-overflow-scrolling: touch;
            height: 100vh;

            &, .app-content__sidebar {
                overflow-y: auto;
                overflow-x: hidden;
                position: relative;
            }

        }

        .app-content__sidebar {
            overflow-y: auto;
            overflow-x: hidden;
            position: relative;

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

            &.app-content__sidebar--visible {
                opacity: 1;
                -webkit-box-flex: 0;
                -ms-flex: 0 0 43rem;
                flex: 0 0 43rem;
                -webkit-transform: translateX(0);
                transform: translateX(0);
            }
        }

        &.search-content {
            .header{
                .header__search--container {
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
                        display: none;
                    }
                }    
            }

        }
    }

`;

export default AppHolder;
