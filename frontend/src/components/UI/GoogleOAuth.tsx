/**
 * Allows users to sign up instantly with Google.
 */

import React from "react";

const GoogleOAuth = () => {
  const handleGoogleSignIn = () => {
    console.log("Google OAuth triggered");
  };

  return (
    <button className="google-btn" onClick={handleGoogleSignIn}>
      🟠 Continue with Google
    </button>
  );
};

export default GoogleOAuth;
