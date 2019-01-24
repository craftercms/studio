export default function reducer(state={
    headerGhost: false
}, action) {

    switch (action.type) {
        case "SET_HEADER_GHOST": {
            return {
                ...state,
                headerGhost: action.payload
            }
        }
        default: {
            return { ...state }
        }
    }
}
