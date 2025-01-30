/**
 * Navbar.tsx
 * 
 * Responsive navigation bar with authentication states and animations.
 */

import React, { useState } from "react";
import { Link } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { useAuth } from "../../hooks/useAuth";
import "../../styles/layout/_navbar.scss";

const Navbar = () => {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  const toggleMenu = () => setMenuOpen(!menuOpen);

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="logo">
          PULSE 🌟
        </Link>

        {/* Desktop Navigation Links */}
        <ul className="nav-links">
          <li><Link to="/explore">Explore</Link></li>
          <li><Link to="/features">Features</Link></li>
          <li><Link to="/about">About</Link></li>
        </ul>

        {/* User Authentication Controls */}
        <div className="auth-controls">
          {user ? (
            <div className="user-menu">
              <button className="user-avatar" onClick={toggleMenu}>
                {user.username[0].toUpperCase()}
              </button>

              <AnimatePresence>
                {menuOpen && (
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
            <>
              <Link to="/login" className="btn-secondary">Login</Link>
              <Link to="/signup" className="btn-primary">Sign Up</Link>
            </>
          )}
        </div>

        {/* Mobile Menu Toggle */}
        <div className="menu-toggle" onClick={toggleMenu}>
          ☰
        </div>

        {/* Mobile Navigation Menu */}
        <AnimatePresence>
          {menuOpen && (
            <motion.ul 
              className="mobile-menu"
              initial={{ x: "100%" }}
              animate={{ x: 0 }}
              exit={{ x: "100%" }}
            >
              <li><Link to="/explore" onClick={toggleMenu}>Explore</Link></li>
              <li><Link to="/features" onClick={toggleMenu}>Features</Link></li>
              <li><Link to="/about" onClick={toggleMenu}>About</Link></li>
              {!user ? (
                <>
                  <li><Link to="/login" onClick={toggleMenu}>Login</Link></li>
                  <li><Link to="/signup" onClick={toggleMenu}>Sign Up</Link></li>
                </>
              ) : (
                <>
                  <li><Link to="/profile" onClick={toggleMenu}>Profile</Link></li>
                  <li onClick={() => { logout(); toggleMenu(); }}>Logout</li>
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
