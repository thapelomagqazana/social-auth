/**
 * CTA.tsx
 * 
 * Displays a call-to-action section with buttons.
 * Includes smooth animations using Framer Motion.
 */

import React from "react";
import { motion } from "framer-motion";
import Button from "../UI/Button";
import "../../assets/styles/landing/_cta.scss";

const CTA = () => {
  return (
    <section className="cta">
      <motion.div 
        className="cta-content"
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
      >
        <h2>Join the Pulse Community Today</h2>
        <p>Share. Connect. Inspire. Sign up now and start your journey!</p>

        {/* CTA Buttons */}
        <div className="cta-buttons">
          <Button to="/signup">🚀 Get Started</Button>
          <Button to="/explore" secondary>
            🔍 Explore
          </Button>
        </div>
      </motion.div>
    </section>
  );
};

export default CTA;
