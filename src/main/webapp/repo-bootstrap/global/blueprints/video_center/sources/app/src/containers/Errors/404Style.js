import styled from 'styled-components';

const NotFoundHolder = styled.div`
    height: 100vh;
    display: flex;
    align-items: center;

    @media (min-width: 768px) {
        height: calc( 100vh - 222px );
        padding-top: 90px;
    }

    .text-container{
        position: relative;
        margin: 0 auto;
        min-height: .1rem;
        width: 110rem;
        max-width: 100%;
        padding: 0 4rem;

        @media (min-width: 75em) {
            width: 110rem;
        }

        @media (min-width: 87.5em) {
            width: 120rem;
        }

        @media (min-width: 114.0625em) {
            width: 135rem;
        }
        
        .heading {
            letter-spacing: 0;
            margin-bottom: 1.5rem;
            font-size: 4rem;
            line-height: 1.1;

            @media (min-width: 75em) {
                font-size: 5rem;
            }
    
            @media (min-width: 87.5em) {
                font-size: 6rem;
            }
    
            @media (min-width: 114.0625em) {
                font-size: 7rem;
            }
        }

        .subtitle {
            margin: 2.5rem 0;
        }
    }
`;

export default NotFoundHolder;
