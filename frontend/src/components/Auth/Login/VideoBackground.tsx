/**
 * Displays a background video for an immersive login experience.
 */
import React from "react";
import "../../../assets/styles/auth/login/_videoBackground.scss";

const VideoBackground = () => {
  return (
    <div className="video-background">
      <video autoPlay loop muted playsInline>
      <source src="https://www.pexels.com/download/video/8439541/" type="video/mp4" />
        Your browser does not support the video tag.
      </video>
    </div>
  );
};

export default VideoBackground;
