/**
 * Checks password strength and ensures passwords match.
 */
import React, { useState } from "react";

interface Props {
  value: string;
  confirmValue: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const PasswordInput: React.FC<Props> = ({ value, confirmValue, onChange }) => {
  const [strength, setStrength] = useState("Weak");

  const checkStrength = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e);
    const password = e.target.value;

    if (password.length > 8) setStrength("Strong");
    else if (password.length > 5) setStrength("Medium");
    else setStrength("Weak");
  };

  return (
    <div className="input-group">
      <input type="password" name="password" value={value} onChange={checkStrength} placeholder="Password" required />
      <span className={`strength ${strength.toLowerCase()}`}>{strength}</span>

      <input type="password" name="confirmPassword" value={confirmValue} onChange={onChange} placeholder="Confirm Password" required />
      {value !== confirmValue && <span className="error-text">Passwords do not match</span>}
    </div>
  );
};

export default PasswordInput;
