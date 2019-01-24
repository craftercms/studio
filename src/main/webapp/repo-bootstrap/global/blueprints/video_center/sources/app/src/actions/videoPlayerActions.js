export function setVideoInfo(player){
    return {
        type: 'SET_VIDEO_INFO',
        payload: player
    }
}

export function setVideoStatus(videoStatus){
    return {
        type: 'SET_VIDEO_STATUS',
        payload: videoStatus
    }
}

export function setVideoDocked(docked){
    return {
        type: 'SET_VIDEO_DOCKED',
        payload: docked
    }
}