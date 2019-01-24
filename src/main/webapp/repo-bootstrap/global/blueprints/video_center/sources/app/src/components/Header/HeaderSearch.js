import React, { Component } from "react";
import { Route } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch } from '@fortawesome/free-solid-svg-icons';

import HeaderSearchModal from './HeaderSearchStyle';
import InputSearch from './searchBox';

class HeaderSearch extends Component {

    state = {
        modalVisible: false
    }

    handleKeyPress = (e, history) => {
        if (e.key === 'Enter') {
          this.setModalVisible( false );
          history.push(`/search/${ e.target.value }`);
        }
    
    };

    setModalVisible( modalVisible ) {
        this.setState({ modalVisible });
    }

    render() {
        return (
            <div className="header__search--container">
                <FontAwesomeIcon className="search__icon" icon={ faSearch } onClick={() => this.setModalVisible(true)}/>

                <HeaderSearchModal
                    style={{ top: 0 }}
                    visible={this.state.modalVisible}
                    onOk={() => this.setModalVisible(false)}
                    onCancel={() => this.setModalVisible(false)}
                    footer={ null }
                    className="header__search--modal"
                    width="100%"
                    closable={ false }
                    destroyOnClose={ true }
                    ref={node => (this.searchModal = node)}
                >
                    <FontAwesomeIcon className="search__icon" icon={ faSearch }/>

                    <Route render={({ history}) => (
                        <InputSearch history={history}
                            handleKeyPress={this.handleKeyPress}
                        />
                    )} />
                    <button className="ant-modal-close" onClick={() => this.setModalVisible(false)}>
                        <span className="ant-modal-close-x"></span>
                    </button>
                </HeaderSearchModal>
            </div>
        );
    }

}

export default HeaderSearch;