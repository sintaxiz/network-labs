import React from "react";
import { NavLink } from "react-router-dom";
import "./place.less"

const Place = (props) => {
    const place = props.place

    return (
        <div className="place">
            <div className="place-header">
                <NavLink to={`/placeinfo/${place.point.lat}/${place.point.lng}`}>
                    <button type="button" class="btn btn-outline-warning">{place.name}</button>
                </NavLink>
            </div>
        </div>
    );
}

export default Place;