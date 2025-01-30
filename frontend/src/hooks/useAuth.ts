/**
 * Custom hook for managing authentication state.
 */

import { useState, useEffect } from "react";
import { signUp, signInWithGoogle } from "../api/auth";

export const useAuth = () => {
  const [user, setUser] = useState<{ username: string; email: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const handleSignUp = async (userData: { username: string; email: string; password: string }) => {
    const newUser = await signUp(userData); // Direct API call
    localStorage.setItem("user", JSON.stringify(newUser));
    setUser(newUser);
  };

  const handleGoogleSignIn = async (googleToken: string) => {
    const newUser = await signInWithGoogle(googleToken); // Direct API call
    localStorage.setItem("user", JSON.stringify(newUser));
    setUser(newUser);
  };

  const logout = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  return { user, loading, handleSignUp, handleGoogleSignIn, logout };
};
