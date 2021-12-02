import axios from "axios";
import { setIsFetching, setPlaces, setQuery, setIsFetchError } from "../reducers/placesReducer";
import { GRAPHHOPPER_KEY } from "./apikeys";

export const getPlaces = (searchQuery) => {
    if (searchQuery == "") {
        return;
    }
    return async (dispatch) => {
        try {
            dispatch(setQuery(searchQuery))
            dispatch(setIsFetching(true))
            const response = await axios.get(`https://graphhopper.com/api/1/geocode?q=${searchQuery}&locale=de&debug=true&key=${GRAPHHOPPER_KEY}`);
            dispatch(setPlaces(response.data))
        } catch (e) {
            dispatch(setIsFetchError(true))
            dispatch(setIsFetching(false))
            setTimeout(() => {
                dispatch(setIsFetchError(false))
            }, 3500)
        }
    }
}