/**
 * Handles API calls for authentication.
 */

import axios from "axios";

// Load API URL from .env
const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:3000/auth";

// Type-safe error handling function
const handleApiError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || "An error occurred";
  }
  return "An unknown error occurred";
};

// Sign-up API request
export const signUp = async (userData: { username: string; email: string; password: string }) => {
  try {
    const response = await axios.post(`${API_URL}/signup`, userData);
    return response.data;
  } catch (error: unknown) {
    throw handleApiError(error);
  }
};

// Google OAuth API request
export const signInWithGoogle = async (googleToken: string) => {
  try {
    const response = await axios.post(`${API_URL}/google-login`, { token: googleToken });
    return response.data;
  } catch (error: unknown) {
    throw handleApiError(error);
  }
};
