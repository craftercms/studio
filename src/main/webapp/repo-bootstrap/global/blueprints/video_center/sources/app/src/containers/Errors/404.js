import React, { Component } from "react";
import { Link } from 'react-router-dom';

import NotFoundHolder from './404Style';

class NotFound extends Component {
    render() {
        return (
            <NotFoundHolder>
                <div className="text-container">
                    <h1 className="heading">Page not Found...</h1>
                    <p className="subtitle">The page you are looking for doesn't exist.</p>
                    <div className="button-container">
                        <Link className="standard-button" to={ "/" }>
                            <span className="standard-button__text">Back to home</span>
                        </Link>
                    </div>
                </div>
            </NotFoundHolder>
        );
    }
}


export default NotFound;
