import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Switch } from 'react-router-dom';
import './app.css'
import PlaceList from './PlaceList';
import PlaceInfo from './placeinfo/PlaceInfo';


const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route exact path='*' element={<PlaceList />} />
        <Route path="/placeinfo/:lat/:lon" element={<PlaceInfo />} />
      </Routes>
    </BrowserRouter >
  );
}

export default App;
