/**
 * Username input field for authentication.
 */

import React from "react";

interface Props {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const UsernameInput: React.FC<Props> = ({ value, onChange }) => {
  return (
    <div className="input-group">
      <input
        type="text"
        name="username"
        value={value}
        onChange={onChange}
        placeholder="Username"
        required
      />
    </div>
  );
};

export default UsernameInput;
