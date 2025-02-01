/**
 * SignUpPage.tsx
 * 
 * The main sign-up page that includes the sign-up form.
 */

import React from "react";
import SignUpForm from "../components/Auth/Register/SignUpForm";
import "../assets/styles/auth/register/_signup.scss";

const SignUpPage = () => {
  return (
    <section className="signup-page">
      <h1>🚀 Join the Community</h1>
      <SignUpForm />
    </section>
  );
};

export default SignUpPage;
