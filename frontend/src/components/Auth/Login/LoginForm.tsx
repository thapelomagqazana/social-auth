/**
 * The main login form with AI-powered username suggestions, 
 * password input, and Google authentication.
 */

import React, { useState } from "react";
import { useAuth } from "../../../hooks/useAuth";
import UsernameInput from "../../UI/UsernameInput";
import PasswordInput from "./PasswordInput";
import GoogleOAuth from "../../UI/GoogleOAuth";
import AuthLinks from "./AuthLinks";
import Button from "../../UI/Button";

const LoginForm = () => {
  // const { handleLogin } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    // await handleLogin({ username, password });
    setLoading(false);
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <UsernameInput value={username} onChange={(e) => setUsername(e.target.value)} />
      <PasswordInput value={password} onChange={setPassword} />

      <Button type="submit" disabled={loading} loading={loading}>
        🔒 Login
      </Button>

      <div className="separator">OR</div>
      <GoogleOAuth />
      <AuthLinks />
    </form>
  );
};

export default LoginForm;
