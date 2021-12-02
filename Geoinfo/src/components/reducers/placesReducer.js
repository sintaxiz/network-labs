const SET_PLACES = "SET_PLACES"
const SET_IS_FETCHING = "SET_IS_FETCHING"
const SET_QUERY = "SET_QUERY"
const SET_IS_FETCH_ERROR = "SET_IS_FETCH_ERROR"

const defaultState = {
    items: [],
    isFetching: false,
    count: 0,
    query: "",
    isFetchError: false
}

// function that changes state of the app
export default function placesReducer(state = defaultState, action) {
    switch (action.type) {
        case SET_PLACES:
            return {
                ...state,
                items: action.payload.hits,
                isFetching: false
            }
        case SET_IS_FETCHING:
            return {
                ...state,
                isFetching: action.payload
            }
        case SET_QUERY:
            return {
                ...state,
                query: action.payload
            }
        case SET_IS_FETCH_ERROR:
            return {
                ...state,
                isFetchError: action.payload
            }
        default:
            return state
    }
}

export const setPlaces = (places) => ({ type: SET_PLACES, payload: places })
export const setIsFetching = (bool) => ({ type: SET_IS_FETCHING, payload: bool })
export const setQuery = (query) => ({ type: SET_QUERY, payload: query })
export const setIsFetchError = (bool) => ({ type: SET_IS_FETCH_ERROR, payload: bool })