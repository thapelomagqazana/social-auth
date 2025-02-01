/**
 * A password input field with an eye-toggle for visibility using React Icons.
 */

import React, { useState } from "react";
import { FiEye, FiEyeOff } from "react-icons/fi";
import "../../../assets/styles/auth/register/_password.scss"
// import "../../assets/styles/auth/_passwordInput.scss";

interface PasswordInputProps {
  value: string;
  onChange: (value: string) => void;
}

const PasswordInput: React.FC<PasswordInputProps> = ({ value, onChange }) => {
  const [visible, setVisible] = useState(false);

  return (
    <div className="password-wrapper">
      <input
        type={visible ? "text" : "password"}
        placeholder="Password"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required
      />
      
      <span className="toggle-btn" onClick={() => setVisible(!visible)}>
        {visible ? <FiEyeOff /> : <FiEye />}
      </span>
    </div>
  );
};

export default PasswordInput;
