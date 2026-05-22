import React, { useState } from 'react';
import './StarRating.css';

interface StarRatingProps {
  rating: number;
  onRatingChange?: (rating: number) => void;
  readonly?: boolean;
  size?: 'small' | 'medium' | 'large';
}

const StarRating: React.FC<StarRatingProps> = ({ 
  rating, 
  onRatingChange, 
  readonly = false, 
  size = 'medium' 
}) => {
  const [hoverRating, setHoverRating] = useState(0);

  const handleStarClick = (starRating: number) => {
    if (!readonly && onRatingChange) {
      onRatingChange(starRating);
    }
  };

  const handleStarHover = (starRating: number) => {
    if (!readonly) {
      setHoverRating(starRating);
    }
  };

  const handleStarLeave = () => {
    if (!readonly) {
      setHoverRating(0);
    }
  };

  const renderStar = (starNumber: number) => {
    const filled = starNumber <= (hoverRating || rating);
    const starClass = `star ${filled ? 'filled' : 'empty'} ${size} ${readonly ? 'readonly' : 'interactive'}`;
    
    return (
      <span
        key={starNumber}
        className={starClass}
        onClick={() => handleStarClick(starNumber)}
        onMouseEnter={() => handleStarHover(starNumber)}
        onMouseLeave={handleStarLeave}
        title={`${starNumber} star${starNumber > 1 ? 's' : ''}`}
      >
        ★
      </span>
    );
  };

  return (
    <div className="star-rating">
      <div className="stars">
        {[1, 2, 3, 4, 5].map(renderStar)}
      </div>
      {!readonly && (
        <div className="rating-text">
          {rating > 0 ? `${rating} star${rating > 1 ? 's' : ''}` : 'Click to rate'}
        </div>
      )}
      {readonly && (
        <div className="rating-text">
          {rating > 0 ? `${rating} star${rating > 1 ? 's' : ''}` : 'Not rated'}
        </div>
      )}
    </div>
  );
};

export default StarRating;
