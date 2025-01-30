/**
 * LandingPage.tsx
 * This is the main landing page of the Pulse social platform.
 * It integrates multiple sections: Hero, Features, Testimonials, and Call-to-Action (CTA).
 */

import React from "react";
import HeroSection from "../components/Landing/HeroSection";
import FeaturesSection from "../components/Landing/FeaturesSection";
import Testimonials from "../components/Landing/Testimonials";
import CTA from "../components/Landing/CTA";

const LandingPage = () => {
  return (
    <div className="landing-page">
      <HeroSection />
      <FeaturesSection />
      <Testimonials />
      <CTA />
    </div>
  );
};

export default LandingPage;
