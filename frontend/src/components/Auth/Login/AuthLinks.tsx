/** 
 * Links for password reset and signup navigation.
 */

import React from "react";
import { Link } from "react-router-dom";
import "../../../assets/styles/auth/login/_authLinks.scss";

const AuthLinks = () => {
  return (
    <div className="auth-links">
      <Link to="/forgot-password">Forgot Password? Reset Here</Link>
      <Link to="/signup">Don't have an account? Sign Up</Link>
    </div>
  );
};

export default AuthLinks;
