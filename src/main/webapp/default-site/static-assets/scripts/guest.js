require(['preview','amplify','communicator'], function () {

    var origin = 'http://127.0.0.1:8080';
    var Events = crafter.studio.preview.Topics;
    var communicator = new crafter.Communicator({ window: window.parent, origin: origin }, origin);

    // When the page has successfully loaded, notify the host window of it's readiness
    // TODO possibly switchable to DOMContentLoaded. Do we need to wait for assets to load?
    // window.addEventListener('load', function () {
        communicator.publish(Events.GUEST_SITE_LOAD, {
            /*  TODO Does the host require anything to be provided by the guest?  */
        });
    // }, false);

});
