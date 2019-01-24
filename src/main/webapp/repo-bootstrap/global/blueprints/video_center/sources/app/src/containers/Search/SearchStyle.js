import styled from 'styled-components';
import { palette } from 'styled-theme';

const SearchHolder = styled.div`

    .search-bar--sticky{
        position: -webkit-sticky;
        position: sticky;
        top: 0;
        z-index: 996;

        .search-bar {
            position: relative;
            overflow: hidden;
            z-index: 998;
            background: ${ palette('primary', 2) };
            pointer-events: none;
            opacity: 0;
            -webkit-transition: opacity 225ms;
            transition: opacity 225ms;

            &.search-bar--visible {
                opacity: 1;
                pointer-events: all;
            }
    
            .search-bar__container {
                position: relative;
                margin: 0 auto;
                min-height: .1rem;
                width: 110rem;
                max-width: 100%;
                padding: 0 4rem;
                height: 7rem;

                @media (min-width: 75em){
                    width: 110rem;
                }

                @media (min-width: 87.5em){
                    width: 120rem;
                }

                @media (min-width: 114.0625em){
                    width: 135rem;
                }

                .search-bar__inner {
                    position: relative;

                    .search-bar__icon {
                        position: absolute;
                        top: 50%;
                        left: 0;
                        margin-top: -2.5rem;
                        line-height: 5rem;

                        .search__icon {
                            font-size: .8em;
                            color: hsla(0,0%,100%,.5);
                        }
                    }

                    .search-bar__input {
                        margin: 0;
                        padding: 0;
                        border: 0;
                        vertical-align: baseline;
                        font-family: inherit;
                        font-size: inherit;
                        font-weight: inherit;
                        line-height: inherit;
                        width: auto;
                        -webkit-appearance: none;
                        -moz-appearance: none;
                        appearance: none;
                        display: block;
                        padding-left: 3.5rem;
                        width: 100%;
                        font-size: 2.2rem;
                        font-weight: 500;
                        color: #e2e3e5;
                        line-height: 7rem;
                        outline: none;
                        border: none;
                        background: none;
                        font-size: 2.6rem;
                    }
                }
            }
        }
    }
`;

export default SearchHolder;
