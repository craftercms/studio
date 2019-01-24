import React, { Component } from "react";
import ModalHolder from "./ModalStyle";

class ModalDialog extends Component {
    state = { visible: false }

    showModal = () => {
        this.setState({
            visible: true,
        });
    }

    handleOk = (e) => {
        this.setState({
            visible: false,
        });
    }

    handleCancel = (e) => {
        this.setState({
            visible: false,
        });
    }

    componentDidMount() {

    }

    render() {
        return (
            <ModalHolder
                title="Share"
                visible={this.state.visible}
                wrapClassName="vertical-center-modal"
                onOk={this.handleOk}
                onCancel={this.handleCancel}
                footer={null}
            >
                { this.props.children }
            </ModalHolder>  
        );
    }
}

export default ModalDialog;