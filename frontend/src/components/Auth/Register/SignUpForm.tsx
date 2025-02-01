/**
 * Handles the sign-up form logic, including AI-powered username suggestions,
 * email validation, password strength meter, and OAuth integration.
 */

import React, { useState } from "react";
import UsernameInput from "../../UI/UsernameInput";
import EmailInput from "./EmailInput";
import PasswordInput from "./PasswordInput";
import GoogleOAuth from "../../UI/GoogleOAuth";
import Button from "../../UI/Button";
// import "../../styles/auth/_signup.scss";

const SignUpForm = () => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Form Submitted:", formData);
    // Submit to backend
  };

  return (
    <form className="signup-form" onSubmit={handleSubmit}>
      <UsernameInput value={formData.username} onChange={handleChange} />
      <EmailInput value={formData.email} onChange={handleChange} />
      <PasswordInput value={formData.password} confirmValue={formData.confirmPassword} onChange={handleChange} />

      <Button type="submit">🟢 Sign Up</Button>

      <div className="or-divider">───────── OR ─────────</div>

      <GoogleOAuth />

      <p className="login-link">
        Already have an account? <a href="/login">Login Here</a>
      </p>
    </form>
  );
};

export default SignUpForm;
