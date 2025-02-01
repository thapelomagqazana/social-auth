/**
 * Validates email format in real time.
 */

import React, { useState } from "react";

interface Props {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const EmailInput: React.FC<Props> = ({ value, onChange }) => {
  const [error, setError] = useState("");

  const validateEmail = (e: React.ChangeEvent<HTMLInputElement>) => {
    const email = e.target.value;
    onChange(e);

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Invalid email format");
    } else {
      setError("");
    }
  };

  return (
    <div className="input-group">
      <input type="email" name="email" value={value} onChange={validateEmail} placeholder="Email" required />
      {error && <span className="error-text">{error}</span>}
    </div>
  );
};

export default EmailInput;
