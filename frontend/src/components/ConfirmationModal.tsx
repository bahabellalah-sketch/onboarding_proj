import React from 'react';

interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  type?: 'danger' | 'warning' | 'info';
  icon?: string;
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  isOpen,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  onConfirm,
  onCancel,
  type = 'danger',
  icon
}) => {
  if (!isOpen) return null;

  const getIcon = () => {
    if (icon) return icon;
    
    switch (type) {
      case 'danger':
        return '🗑️';
      case 'warning':
        return '⚠️';
      case 'info':
        return 'ℹ️';
      default:
        return '🗑️';
    }
  };

  const getTypeClass = () => {
    switch (type) {
      case 'danger':
        return 'confirmation-modal--danger';
      case 'warning':
        return 'confirmation-modal--warning';
      case 'info':
        return 'confirmation-modal--info';
      default:
        return 'confirmation-modal--danger';
    }
  };

  return (
    <div className={`confirmation-modal ${getTypeClass()}`}>
      <div className="confirmation-content">
        <div className="confirmation-icon">{getIcon()}</div>
        <h2 className="confirmation-title">{title}</h2>
        <p className="confirmation-message">{message}</p>
        <div className="confirmation-actions">
          <button 
            onClick={onConfirm} 
            className={`confirm-btn confirm-btn--${type}`}
          >
            {confirmText}
          </button>
          <button onClick={onCancel} className="cancel-btn">
            {cancelText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
