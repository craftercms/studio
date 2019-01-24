import React, { Component } from 'react';
import { Input } from 'antd';
class InputSearch extends Component {
    componentDidMount() {
        setTimeout(() => {
            try {
                document.getElementById('InputHeaderSearch').focus();
            } catch (e) {}
        }, 200);
    }
    render() {
        return (
            <Input placeholder="Start Typing..." 
                id="InputHeaderSearch"
                onKeyPress={(e) => this.props.handleKeyPress(e, this.props.history)}
            />
        );
    }
}

export default InputSearch;