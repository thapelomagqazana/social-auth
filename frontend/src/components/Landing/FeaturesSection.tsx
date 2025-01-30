/**
 * FeaturesSection.tsx
 * 
 * Displays key features of the platform with a horizontal scrolling layout on mobile.
 */

import React from "react";
import { motion } from "framer-motion";
import "../../assets/styles/landing/_features.scss";

const features = [
  { title: "🌍 Global Community", desc: "Connect with creators worldwide." },
  { title: "📸 Image Sharing", desc: "Post and discover stunning visuals." },
  { title: "🖤 Real-time Engagement", desc: "Like, comment, and interact instantly." },
//   { title: "🔒 Secure & Private", desc: "Your data is always protected." },
];

const FeaturesSection = () => {
  return (
    <section className="features">
      <h2>Why Choose Pulse?</h2>

      {/* Horizontal Scroll Wrapper */}
      <motion.div
        className="features-scroll-wrapper"
        whileTap={{ cursor: "grabbing" }} // Allows dragging on mobile
      >
        <div className="features-grid">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              className="feature-card"
              whileHover={{ scale: 1.05 }}
              transition={{ duration: 0.3 }}
            >
              <h3>{feature.title}</h3>
              <p>{feature.desc}</p>
            </motion.div>
          ))}
        </div>
      </motion.div>
    </section>
  );
};

export default FeaturesSection;
