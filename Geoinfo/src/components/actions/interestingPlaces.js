import axios from "axios";
import { setIsFetchError } from "../reducers/placesReducer";
import { OPENTRIMAP_KEY } from "./apikeys";

export const getInterestingPlaces = (lat, lon, setInterestingPlaces) => {
    const radius = 50000 // in meters
    return async (dispatch) => {
        try {
            const response = await axios.get(`https://api.opentripmap.com/0.1/en/places/radius?radius=${radius}&lon=${lon}&lat=${lat}&apikey=${OPENTRIMAP_KEY}`,
                {
                    headers: {
                        Accept: "application/json"
                    }
                }
            );
            setInterestingPlaces(response.data.features)
        } catch (e) {
            dispatch(setIsFetchError(true))
            setTimeout(() => {
                dispatch(setIsFetchError(false))
            }, 3500)
        }
    }
}