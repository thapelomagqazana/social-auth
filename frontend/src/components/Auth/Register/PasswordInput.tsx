/**
 * A password input component with strength checking, confirmation, 
 * and visibility toggle using react-icons.
 */

import React, { useState } from "react";
import { FiEye, FiEyeOff } from "react-icons/fi";

interface Props {
  value: string;
  confirmValue: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const PasswordInput: React.FC<Props> = ({ value, confirmValue, onChange }) => {
  const [strength, setStrength] = useState("Weak");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Toggle visibility for password fields
  const togglePasswordVisibility = () => setShowPassword(!showPassword);
  const toggleConfirmPasswordVisibility = () => setShowConfirmPassword(!showConfirmPassword);

  const checkStrength = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e);
    const password = e.target.value;

    if (password.length > 8) setStrength("Strong");
    else if (password.length > 5) setStrength("Medium");
    else setStrength("Weak");
  };

  return (
    <div className="input-group">
      {/* Password Input Field with Visibility Toggle */}
      <div className="password-wrapper">
        <input
          type={showPassword ? "text" : "password"}
          name="password"
          value={value}
          onChange={checkStrength}
          placeholder="Password"
          required
        />
        <button type="button" className="toggle-btn" onClick={togglePasswordVisibility}>
          {showPassword ? <FiEyeOff /> : <FiEye />}
        </button>
      </div>
      <span className={`strength ${strength.toLowerCase()}`}>{strength}</span>

      {/* Confirm Password Input Field with Visibility Toggle */}
      <div className="password-wrapper">
        <input
          type={showConfirmPassword ? "text" : "password"}
          name="confirmPassword"
          value={confirmValue}
          onChange={onChange}
          placeholder="Confirm Password"
          required
        />
        <button type="button" className="toggle-btn" onClick={toggleConfirmPasswordVisibility}>
          {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
        </button>
      </div>

      {/* Password Mismatch Warning */}
      {value !== confirmValue && <span className="error-text">Passwords do not match</span>}
    </div>
  );
};

export default PasswordInput;
