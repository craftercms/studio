export function updateDimensions() {
    var playerContainer = document.getElementById("app-content__player"),
        playerAspect = document.querySelector(".global-video-player .player-container"),
        playerResize = document.querySelector(".global-video-player"),
        dimensions = {
            width: playerContainer.offsetWidth,
            height: playerContainer.offsetHeight
        },
        aspect = ( playerAspect.offsetHeight * 100 ) / playerAspect.offsetWidth,
        maxWidth = ( dimensions.height * 100 ) / aspect;

    playerResize.style.minWidth = "160px";
    playerResize.style.minHeight = "90px";
    playerResize.style.maxWidth =  maxWidth + "px";
    playerResize.style.maxHeight = dimensions.height + "px";
}