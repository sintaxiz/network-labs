import axios from "axios";
import { setIsFetchError } from "../reducers/placesReducer";
import { OPENWETHERMAP_KEY } from "./apikeys";

export const getWeather = (lat, lon, setWeather) => {
    return async () => {
        try {
            const response = await axios.get(`https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${OPENWETHERMAP_KEY}`)
            setWeather(response.data)
        } catch (e) {
            dispatch(setIsFetchError(true))
            setTimeout(() => {
                dispatch(setIsFetchError(false))
            }, 3500)
        }
    }
}