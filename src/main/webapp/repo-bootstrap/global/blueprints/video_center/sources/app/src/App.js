import React, { Component } from "react";
import Router from './Router';
import { ThemeProvider } from 'styled-components';
import themes from './settings/themes';
import { themeConfig } from './settings';
import AppHolder from './AppStyle';
import { Provider } from "react-redux";
import store from "./store"; 

import Header from "./components/Header/Header.js";
import Footer from "./components/Footer/Footer.js";
import VideoPlayer from "./components/VideoPlayer/VideoPlayer.js";

class App extends Component {
    render() {
        return (
            <ThemeProvider theme={themes[themeConfig.theme]}>
                <Provider store={store}>
                    <AppHolder className="app">
                        <div className="app-content">
                            <div className="app-content__cont" id="app-content">
                                <div className="app-content__main">
                                    <Header/>

                                    <div id="app-content-player">
                                        <VideoPlayer/>
                                    </div>

                                    <Router/>

                                    <Footer/>
                                </div>
                            </div>
                        </div>
                    </AppHolder>
                </Provider>
            </ThemeProvider>
        );
    }
}

export default App
