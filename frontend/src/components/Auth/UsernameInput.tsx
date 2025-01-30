/**
 * AI-powered username suggestion feature.
 */

import React, { useState } from "react";

interface Props {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const UsernameInput: React.FC<Props> = ({ value, onChange }) => {
  const [suggestions, setSuggestions] = useState(["coolUser123", "devKing99", "pulseUser"]);

  return (
    <div className="input-group">
      <input type="text" name="username" value={value} onChange={onChange} placeholder="Username" required />
      {value && (
        <div className="suggestions">
          <p>Suggestions:</p>
          {suggestions.map((s, i) => (
            <span key={i} onClick={() => onChange({ target: { name: "username", value: s } } as any)}>
              {s}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

export default UsernameInput;
