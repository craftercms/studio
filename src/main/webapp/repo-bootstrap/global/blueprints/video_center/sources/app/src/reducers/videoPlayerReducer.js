export default function reducer(state={
    videoInfo: null,
    videoStatus: {
        loaded: false,
        playing: false,
        docked: false,
        currentVideoUrl: ''
    }
}, action) {

    switch (action.type) {
        case "GET_VIDEO_INFO": {
            return { ...state }
        }
        case "SET_VIDEO_INFO": {
            return {
                ...state,
                videoInfo: action.payload
            }
        }
        case "SET_VIDEO_STATUS": {
            return {
                ...state,
                videoStatus: action.payload
            }
        }
        case "SET_VIDEO_DOCKED": {
            return {
                ...state,
                videoStatus: { ...state.videoStatus, docked: action.payload }
            }
        }
        case "SET_SHOW_PORTAL": {
            return {
                ...state,
                showPortal: action.payload
            }
        }
        default: {
            return { ...state }
        }
    }
}
