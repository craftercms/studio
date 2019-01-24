import styled from 'styled-components';
import { palette } from 'styled-theme';

const FooterHolder = styled.div`
    position: relative;

    .footer {
        position: relative;
        background: ${ palette('primary', 1) };
        margin-top: 10rem;
        width: 100%;
        padding: 50px;
        
        .footer__content {
            display: -webkit-box;
            display: -ms-flexbox;
            display: flex;
            -webkit-box-orient: horizontal;
            -webkit-box-direction: normal;
            -ms-flex-direction: row;
            flex-direction: row;
            -webkit-box-pack: justify;
            -ms-flex-pack: justify;
            justify-content: space-between;
            -ms-flex-wrap: nowrap;
            flex-wrap: nowrap;
            color: #8b9099;
            font-size: 1.1rem;
            font-family: inherit;
            font-weight: 800;
            // text-transform: uppercase;
            line-height: 2;

            .footer__copyright {
                white-space: nowrap;
            }

            .footer__nav {
                text-align: right;

                .footer__link {
                    &, :focus {
                        color: #8b9099;
                        padding-left: 25px;
                    }

                    &:hover {
                        text-decoration: underline;
                    }
                }
            }
        }
    }
`;

export default FooterHolder;
