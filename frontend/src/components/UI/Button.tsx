/**
 * Button.tsx
 * 
 * A reusable button component with primary and secondary styles.
 * Supports loading state, disabled state, and link navigation.
 */

import React from "react";
import { Link } from "react-router-dom";
// import "../../styles/ui/_button.scss";

interface ButtonProps {
  to?: string; // Optional link navigation
  children: React.ReactNode;
  secondary?: boolean; // Secondary style
  disabled?: boolean;
  loading?: boolean;
  onClick?: () => void;
}

const Button: React.FC<ButtonProps> = ({ to, children, secondary, disabled, loading, onClick }) => {
  const className = `btn ${secondary ? "btn-secondary" : "btn-primary"} ${disabled ? "btn-disabled" : ""}`;

  if (to) {
    return (
      <Link to={to} className={className}>
        {loading ? <span className="spinner"></span> : children}
      </Link>
    );
  }

  return (
    <button className={className} onClick={onClick} disabled={disabled || loading}>
      {loading ? <span className="spinner"></span> : children}
    </button>
  );
};

export default Button;
