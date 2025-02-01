/**
 * Full page layout for the login form, background video, and authentication options.
 */

import React from "react";
import LoginForm from "../components/Auth/Login/LoginForm";
import VideoBackground from "../components/Auth/Login/VideoBackground";
import "../assets/styles/auth/login/_login.scss";

const LoginPage = () => {
  return (
    <div className="login-page">
      <VideoBackground />
      <div className="login-container">
        <h2>Welcome Back!</h2>
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;
