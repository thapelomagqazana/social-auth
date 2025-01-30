/**
 * Testimonials.tsx
 * 
 * Displays user testimonials with horizontal scrolling on mobile.
 * Includes smooth animations using Framer Motion.
 */

import React from "react";
import { motion } from "framer-motion";
import "../../assets/styles/landing/_testimonials.scss";

// Dummy testimonial data
const testimonials = [
  { user: "Alice M.", text: "Pulse changed the way I connect with others!", image: "/assets/users/alice.jpg" },
  { user: "David K.", text: "The best social platform for creators.", image: "/assets/users/david.jpg" },
  { user: "Emma T.", text: "Love the design and features. Super smooth!", image: "/assets/users/emma.jpg" },
//   { user: "Michael R.", text: "Highly recommend for networking!", image: "/assets/users/michael.jpg" },
];

const Testimonials = () => {
  return (
    <section className="testimonials">
      <motion.h2 
        className="testimonials-title"
        initial={{ opacity: 0, y: -30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
      >
        What Our Users Say
      </motion.h2>

      {/* Horizontal Scroll Wrapper for Mobile */}
      <motion.div className="testimonials-scroll-wrapper" whileTap={{ cursor: "grabbing" }}>
        <div className="testimonial-grid">
          {testimonials.map((review, index) => (
            <motion.div
              key={index}
              className="testimonial-card"
              whileHover={{ scale: 1.05 }}
              transition={{ duration: 0.3 }}
            >
              <img src={review.image} alt={review.user} className="user-image" />
              <p className="testimonial-text">"{review.text}"</p>
              <span className="testimonial-user">- {review.user}</span>
            </motion.div>
          ))}
        </div>
      </motion.div>
    </section>
  );
};

export default Testimonials;
