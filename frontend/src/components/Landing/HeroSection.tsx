/**
 * HeroSection.tsx
 * The main hero banner of the landing page.
 * Displays an immersive background, tagline, and action buttons.
 */

import React from "react";
import { motion } from "framer-motion";
import Button from "../UI/Button";
import "../../assets/styles/landing/_hero.scss";

const HeroSection = () => {
  return (
    <section className="hero">
      {/* Hero Content */}
      <motion.div 
        className="hero-content"
        initial={{ opacity: 0, y: -50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 1 }}
      >
        <h1 className="hero-title">Welcome to Pulse 🌟</h1>
        <p className="hero-subtitle">Share. Connect. Inspire.</p>
        
        {/* CTA Buttons */}
        <motion.div
          className="hero-buttons"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1, delay: 0.5 }}
        >
          <Button to="/signup">🚀 Get Started</Button>
          <Button to="/login" secondary>
            🔐 Login
          </Button>
        </motion.div>
      </motion.div>
    </section>
  );
};

export default HeroSection;
