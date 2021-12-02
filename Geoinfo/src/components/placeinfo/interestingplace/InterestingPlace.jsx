import "./interestingplace.less";
import React, { useState, } from "react";
import { getDescription } from "../../actions/description";
import { useDispatch } from 'react-redux'


const InterestingPlace = (props) => {
    const place = props.place
    const index = props.index
    const [description, setDescription] = useState({})
    const dispatch = useDispatch()

    var showImage = false
    var imgUrl = ""

    function getPlaceDesc(xid) {
        dispatch(getDescription(xid, setDescription))
        console.log(description)
        return description
    };

    return (
        <div className="place">
            <div>
                <p>{index + 1}) {place.properties.name}</p>
                <button type="button" class="btn btn-outline-primary mr-1" onClick={() => {
                    var placeDesc = getPlaceDesc(place.properties.xid)
                    var descText = placeDesc.hasOwnProperty("wikipedia_extracts") ? placeDesc.wikipedia_extracts.text : "no description :("
                    window.alert(placeDesc.name + '\r\n' + descText)
                    imgUrl = placeDesc.image
                    showImage = true

                }
                }>show more info</button>
                {showImage &&
                    <img src={imgUrl} />
                }
            </div>
        </div >
    );
}

export default InterestingPlace;