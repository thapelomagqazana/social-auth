import React from 'react';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from './components/UI/Navbar';
import LandingPage from './pages/LandingPage';
import SignUpPage from './pages/SignUpPage';
import LoginPage from './pages/LoginPage';
import "./assets/styles/global.scss"; // Global styles

function App() {
  return (
    <>
      <Router>
        {/* <ThemeToggle /> */}
        <Navbar />
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          {/* <Route path="/reset-password" element={<ForgotPasswordPage />} /> */}
        </Routes>
      </Router>
    </>
  );
}

export default App;
