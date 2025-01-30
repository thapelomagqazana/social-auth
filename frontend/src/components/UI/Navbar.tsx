/**
 * Responsive navigation bar with animations and authentication states.
 */

import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { useAuth } from "../../hooks/useAuth";
import Button from "../UI/Button";
import "../../assets/styles/layout/_navbar.scss";

const Navbar = () => {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const menuRef = useRef<HTMLUListElement>(null);
  const dropdownRef = useRef<HTMLUListElement>(null);

  // ✅ Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <nav className="navbar">
      <div className="nav-container">
        {/* Logo */}
        <Link to="/" className="logo">
          PULSE<span className="logo-highlight">🌟</span>
        </Link>

        {/* Desktop Navigation Links */}
        <ul className="nav-links">
          <li><Link to="/explore">Explore</Link></li>
          <li><Link to="/features">Features</Link></li>
          <li><Link to="/about">About</Link></li>
        </ul>

        {/* Authentication & User Menu */}
        <div className="auth-controls">
          {user ? (
            <div className="user-menu" ref={dropdownRef}>
              <button className="user-avatar" onClick={() => setDropdownOpen(!dropdownOpen)}>
                {user.username[0].toUpperCase()}
              </button>

              <AnimatePresence>
                {dropdownOpen && (
                  <motion.ul 
                    className="dropdown-menu"
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                  >
                    <li><Link to="/profile">Profile</Link></li>
                    <li onClick={logout}>Logout</li>
                  </motion.ul>
                )}
              </AnimatePresence>
            </div>
          ) : (
            <div className="auth-buttons">
              <Button to="/signup">Sign Up</Button>
              <Button to="/login" secondary>
                Login
              </Button>
            </div>
          )}
        </div>

        {/* Mobile Menu Toggle */}
        <button className="menu-toggle" onClick={() => setMenuOpen(!menuOpen)}>
          ☰
        </button>

        {/* Mobile Navigation Menu */}
        <AnimatePresence>
          {menuOpen && (
            <motion.ul 
              className="mobile-menu"
              ref={menuRef}
              initial={{ x: "100%" }}
              animate={{ x: 0 }}
              exit={{ x: "100%" }}
            >
              <li><Link to="/explore" onClick={() => setMenuOpen(false)}>Explore</Link></li>
              <li><Link to="/features" onClick={() => setMenuOpen(false)}>Features</Link></li>
              <li><Link to="/about" onClick={() => setMenuOpen(false)}>About</Link></li>
              {!user ? (
                <>
                  <li><Link to="/login" onClick={() => setMenuOpen(false)}>Login</Link></li>
                  <li><Link to="/signup" onClick={() => setMenuOpen(false)}>Sign Up</Link></li>
                </>
              ) : (
                <>
                  <li><Link to="/profile" onClick={() => setMenuOpen(false)}>Profile</Link></li>
                  <li onClick={() => { logout(); setMenuOpen(false); }}>Logout</li>
                </>
              )}
            </motion.ul>
          )}
        </AnimatePresence>
      </div>
    </nav>
  );
};

export default Navbar;
