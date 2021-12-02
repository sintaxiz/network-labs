
import axios from "axios";
import { OPENTRIMAP_KEY } from "./apikeys";
import { setIsFetchError } from "../reducers/placesReducer";


export const getDescription = (xid, setDescription) => {
    return async (dispatch) => {
        try {
            const response = await axios.get(`https://api.opentripmap.com/0.1/en/places/xid/${xid}?apikey=${OPENTRIMAP_KEY}`)
            console.log(response.data)
            setDescription(response.data)
        } catch (e) {
            dispatch(setIsFetchError(true))
            setTimeout(() => {
                dispatch(setIsFetchError(false))
            }, 3500)
        }

    }
}