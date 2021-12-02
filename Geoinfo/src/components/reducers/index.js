import { combineReducers } from "redux";
import { createStore, applyMiddleware } from "redux";
import placesReducer from "./placesReducer";
import { composeWithDevTools } from "redux-devtools-extension";
import thunk from "redux-thunk";

const rootReducer = combineReducers({
    places: placesReducer,
})

export const store = createStore(rootReducer, composeWithDevTools(applyMiddleware(thunk)))