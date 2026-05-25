import React from 'react';

interface StarRatingInputProps {
  value: number;
  onChange: (value: number) => void;
  readonly?: boolean;
}

const StarRatingInput: React.FC<StarRatingInputProps> = ({ value, onChange, readonly = false }) => (
  <div className="star-rating-input">
    {[1, 2, 3, 4, 5].map((star) => (
      <button
        key={star}
        type="button"
        className={`star-rating-input__star ${star <= value ? 'star-rating-input__star--on' : ''}`}
        onClick={() => !readonly && onChange(star)}
        disabled={readonly}
        aria-label={`${star} étoile${star > 1 ? 's' : ''}`}
      >
        ★
      </button>
    ))}
  </div>
);

export default StarRatingInput;
