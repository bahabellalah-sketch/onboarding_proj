import React, { useState, useEffect, useCallback } from 'react';
import { evaluationApi, Evaluation, EvaluationRequest } from '../services/evaluationApi';
import { useAuth } from '../contexts/AuthContext';
import '../styles/DarkRedTheme.css';

interface EvaluationComponentProps {
  checklistId: number;
  checklistTitle: string;
  checklistStatus: string;
  onEvaluationSubmitted?: () => void;
}

const EvaluationComponent: React.FC<EvaluationComponentProps> = ({
  checklistId,
  checklistTitle,
  checklistStatus,
  onEvaluationSubmitted
}) => {
  const { user } = useAuth();
  const [canEvaluate, setCanEvaluate] = useState<boolean>(false);
  const [existingEvaluation, setExistingEvaluation] = useState<Evaluation | null>(null);
  const [rating, setRating] = useState<number>(0);
  const [comment, setComment] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [success, setSuccess] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const checkEvaluationStatus = useCallback(async () => {
    console.log('DEBUG: checkEvaluationStatus called');
    console.log('DEBUG: User:', user);
    console.log('DEBUG: Checklist ID:', checklistId);
    
    // Add verification that component is running
    console.log('DEBUG: Component is running - Checklist ID:', checklistId, 'User:', user?.role);
    console.log('DEBUG: Full user object:', user);
    console.log('DEBUG: User ID type:', typeof user?.id);
    console.log('DEBUG: User ID value:', user?.id);
    
    // Try to get user ID from different sources
    const userId = user?.id;
    console.log('DEBUG: Final userId to use:', userId);
    
    if (!userId) {
      console.log('DEBUG: No user ID found, returning');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      console.log('DEBUG: Checking if user can evaluate');
      // Check if user can evaluate
      const canEval = await evaluationApi.canUserEvaluate(checklistId, userId);
      console.log('DEBUG: Can evaluate result:', canEval);
      setCanEvaluate(canEval);

      console.log('DEBUG: Getting existing evaluation');
      // Get existing evaluation
      const existing = await evaluationApi.getEvaluationByUserAndChecklist(userId, checklistId);
      console.log('DEBUG: Existing evaluation:', existing);
      setExistingEvaluation(existing);

      if (existing) {
        setRating(existing.rating);
        setComment(existing.comment || '');
      }
    } catch (error) {
      console.error('Error checking evaluation status:', error);
      setCanEvaluate(false);
    } finally {
      setIsLoading(false);
    }
  }, [user, checklistId]);

  // Check evaluation status
  useEffect(() => {
    if (user && checklistId) {
      checkEvaluationStatus();
    }
  }, [user, checklistId, checkEvaluationStatus]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.id) {
      setError('You must be logged in to submit an evaluation');
      return;
    }
    if (rating === 0) {
      setError('Please select a rating');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      const evaluationRequest: EvaluationRequest = {
        checklistId,
        userId: user.id,
        rating,
        comment: comment.trim() || undefined
      };

      await evaluationApi.createEvaluation(evaluationRequest);
      setSuccess(true);
      
      if (onEvaluationSubmitted) {
        onEvaluationSubmitted();
      }

      // Reset form after successful submission
      setTimeout(() => {
        setSuccess(false);
        checkEvaluationStatus();
      }, 3000);

    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Failed to submit evaluation';
      setError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const StarRating = ({ value, onChange, readonly = false }: { value: number; onChange: (value: number) => void; readonly?: boolean }) => {
    return (
      <div className="star-rating">
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            className={`star ${star <= value ? 'filled' : ''}`}
            onClick={() => !readonly && onChange(star)}
            style={{ cursor: readonly ? 'default' : 'pointer' }}
          >
            ★
          </span>
        ))}
      </div>
    );
  };

  // Show "Already Evaluated" message if user already evaluated
  if (existingEvaluation) {
    return (
      <div className="evaluation-component already-evaluated">
        <h4>✅ Déjà évalué</h4>
        <div className="evaluation-info">
          <div className="rating-display">
            <p><strong>Note :</strong></p>
            <div className="star-rating readonly">
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  className={`star ${star <= existingEvaluation.rating ? 'filled' : ''}`}
                >
                  ★
                </span>
              ))}
            </div>
          </div>
          {existingEvaluation.comment && (
            <div className="comment-display">
              <p><strong>Commentaire :</strong></p>
              <div className="comment-content">
                {existingEvaluation.comment}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // Show loading state
  if (isLoading) {
    return (
      <div className="evaluation-component loading">
        <p>Chargement…</p>
      </div>
    );
  }

  // Only collaborators can evaluate their own checklists
  const isCollaborator = user?.role === 'COLLABORATEUR';
  const canEvaluateAsCollaborator = canEvaluate && checklistStatus === 'TERMINE' && isCollaborator;
  
  // Show evaluation form only for collaborators
  if (canEvaluateAsCollaborator) {
    return (
      <div className="evaluation-component">
        <h4>Évaluer l&apos;étape : {checklistTitle}</h4>
        
        {success && (
          <div className="success-message">
            ✅ Évaluation enregistrée !
          </div>
        )}

        {error && (
          <div className="error-message">
            ❌ {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="evaluation-form">
          <div className="form-group">
            <label>Note *</label>
            <StarRating value={rating} onChange={setRating} />
          </div>

          <div className="form-group">
            <label>Commentaire</label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Votre avis sur cette étape…"
              rows={3}
            />
          </div>

          <button type="submit" disabled={isSubmitting} className="btn btn-primary">
            {isSubmitting ? 'Envoi…' : 'Envoyer l\'évaluation'}
          </button>
        </form>
      </div>
    );
  }

  return null;
};

export default EvaluationComponent;
