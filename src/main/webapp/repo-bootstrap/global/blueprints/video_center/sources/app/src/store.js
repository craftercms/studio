import { createReduxStore } from '@craftercms/redux';
import { crafterConf } from '@craftercms/classes';
import { studioConfig } from './settings';

import thunk from "redux-thunk";

import { allReducers } from "./reducers";

crafterConf.configure({
    site: studioConfig.site,
    baseUrl: studioConfig.baseUrl
})

const store = createReduxStore({
    namespace: "craftercms",
    namespaceCrafterState: true,
    reducerMixin: allReducers,
    reduxDevTools: true,
    additionalMiddleWare: [thunk]
});

export default store;