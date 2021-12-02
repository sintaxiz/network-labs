import React, { Component, useEffect, useState } from 'react'
import { render } from 'react-dom'
import { useDispatch, useSelector } from 'react-redux'
import Place from './place/Place'
import { getPlaces } from './actions/places'
import "./placelist.less"

const PlaceList = () => {
    const dispatch = useDispatch()
    const places = useSelector(state => state.places.items)
    const isFetching = useSelector(state => state.places.isFetching)
    const query = useSelector(state => state.places.query)
    const isFetchError = useSelector(state => state.places.isFetchError)
    const [searchValue, setSearchValue] = useState("")

    function handleInput(e) {
        setSearchValue(e.target.value)
        dispatch(getPlaces(searchValue))
    }
    return (
        <div class="container">
            {isFetchError &&
                <div class="alert alert-danger" role="alert">
                    Error! Can not show info :C
                </div>
            }
            <div className="lenny">
                ( ͡° ͜ʖ ͡°) ну давай давай
            </div>

            <div className="search" >
                <input class="form-control" type="text" className="search-input" placeholder="Search places" onChange={handleInput} value={searchValue} />
            </div>
            {
                isFetching == false ?
                    places.map(place =>
                        <Place place={place} />) :
                    <div className="fetching">
                        loading...
                    </div>
            }
        </div>
    )
}

export default PlaceList;