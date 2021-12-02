import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux'
import { getWeather } from "../actions/weather";
import { getInterestingPlaces } from "../actions/interestingPlaces";
import "./placeinfo.less"
import InterestingPlace from "./interestingplace/InterestingPlace";

const PlaceInfo = () => {
    const dispatch = useDispatch()
    const navigate = useNavigate()
    const { lat, lon } = useParams()
    const [weather, setWeather] = useState({ weather: [{ main: "main", description: "desc" }], main: "" })
    const [interestingPlaces, setInterestingPlaces] = useState([])
    const isFetchError = useSelector(state => state.places.isFetchError)


    useEffect(() => {
        dispatch(getWeather(lat, lon, setWeather))
        dispatch(getInterestingPlaces(lat, lon, setInterestingPlaces))
    }, [])
    //   const [description, setDescription] = useState({ wikipedia_extracts: { text: "" } })


    // navigate(-1) -- back 1 page
    return (
        <div className="place-info">
            {isFetchError &&
                <div class="alert alert-danger" role="alert">
                    Error! Can not show info :C
                </div>
            }
            <button onClick={() => navigate(-1)} className="back-button" type="button" class="btn btn-outline-secondary">back to list</button>
            <div>
                <h1>Weather in {weather.name}</h1>
                <b>{weather.weather[0].main}</b>
                <p>{weather.weather[0].description}</p>
                <p>actual temp: {weather.main.temp}</p>
                <p>feels like: {weather.main.feels_like}</p>
            </div>
            <div>
                <h1>List of interesting places in radius of 50km</h1>
                {interestingPlaces.map((place, index) =>
                    <InterestingPlace place={place} index={index} />
                )}
            </div>
        </div>
    )
}

export default PlaceInfo;